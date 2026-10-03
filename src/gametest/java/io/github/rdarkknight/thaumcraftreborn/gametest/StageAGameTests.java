package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugData;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugDataEntry;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.config.CommonConfig;
import io.github.rdarkknight.thaumcraftreborn.core.config.ConfigFiles;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxPayload;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import io.netty.buffer.Unpooled;

public final class StageAGameTests implements CustomTestMethodInvoker {
	@GameTest
	public void fxRegistryIsSyncedAndContainsDebugBurst(GameTestHelper context) {
		context.assertTrue(
				RegistryAttributeHolder.get(ThaumcraftRegistryKeys.FX_TYPE).hasAttribute(RegistryAttribute.SYNCED),
				"FX registry has SYNCED attribute"
		);
		context.assertTrue(
				ThaumcraftRegistries.FX_TYPE.getKey(DebugFx.DEBUG_BURST).equals(ThaumcraftRebornApi.id("debug_burst")),
				"FX registry contains debug_burst"
		);
		context.succeed();
	}

	@GameTest
	public void fxPayloadRoundTripsWithRegistryAccess(GameTestHelper context) {
		FxPayload original = FxPayload.of(DebugFx.DEBUG_BURST, 19, new net.minecraft.world.phys.Vec3(1.25, 64.5, -8.75));
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.getLevel().registryAccess());
		FxPayload.STREAM_CODEC.encode(buffer, original);
		FxPayload decoded = FxPayload.STREAM_CODEC.decode(buffer);
		context.assertTrue(decoded.fxType() == DebugFx.DEBUG_BURST, "FX type round trip");
		context.assertTrue(decoded.data().equals(19), "FX data round trip");
		context.assertTrue(decoded.origin().equals(original.origin()), "FX origin round trip");
		buffer.release();
		context.succeed();
	}

	@GameTest
	public void syncedDataLoaderSkipsInvalidEntries(GameTestHelper context) {
		DebugDataEntry sample = DebugData.ENTRIES.serverEntries().get(ThaumcraftRebornApi.id("sample"));
		context.assertTrue(sample != null && sample.label().equals("sample") && sample.value() == 7, "sample entry is loaded");
		context.assertTrue(!DebugData.ENTRIES.serverEntries().containsKey(ThaumcraftRebornApi.id("invalid")), "invalid entry is skipped");
		context.succeed();
	}

	@GameTest
	public void commonConfigDefaultsRoundTripAndPreservesInvalidFile(GameTestHelper context) throws IOException {
		CommonConfig parsedDefaults = CommonConfig.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow();
		context.assertTrue(parsedDefaults.equals(CommonConfig.DEFAULT), "empty config uses defaults");
		var encoded = CommonConfig.CODEC.encodeStart(JsonOps.INSTANCE, parsedDefaults).getOrThrow();
		context.assertTrue(CommonConfig.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().equals(parsedDefaults), "config codec round trip");

		Path directory = Files.createTempDirectory("thaumcraft-reborn-config-test");
		Path invalidFile = directory.resolve("invalid.json");
		String invalidContents = "{ invalid";
		try {
			Files.writeString(invalidFile, invalidContents);
			CommonConfig loaded = ConfigFiles.load(invalidFile, CommonConfig.CODEC, CommonConfig.DEFAULT);
			context.assertTrue(loaded.equals(CommonConfig.DEFAULT), "invalid config uses defaults");
			context.assertTrue(Files.readString(invalidFile).equals(invalidContents), "invalid config file remains unchanged");
		} finally {
			Files.deleteIfExists(invalidFile);
			Files.deleteIfExists(directory);
		}
		context.succeed();
	}

	@GameTest
	public void debugContainerPersistsItemsAndTicks(GameTestHelper context) {
		BlockPos pos = BlockPos.ZERO;
		BlockState state = DebugContent.DEBUG_CONTAINER.defaultBlockState();
		DebugContainerBlockEntity original = new DebugContainerBlockEntity(pos, state);
		original.setItem(0, new ItemStack(Items.DIAMOND, 4));
		for (int tick = 0; tick < 37; tick++) {
			DebugContainerBlockEntity.serverTick(context.getLevel(), pos, state, original);
		}

		CompoundTag saved = original.saveWithoutMetadata(context.getLevel().registryAccess());
		DebugContainerBlockEntity restored = new DebugContainerBlockEntity(pos, state);
		restored.loadWithComponents(TagValueInput.create(
				ProblemReporter.DISCARDING,
				context.getLevel().registryAccess(),
				saved
		));
		context.assertTrue(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 4, "container item is restored");
		context.assertTrue(restored.ticks() == 37, "tick count is restored");
		context.succeed();
	}

	@GameTest
	public void debugContainerDropsItemsWhenRemoved(GameTestHelper context) {
		BlockPos relativePos = new BlockPos(1, 1, 1);
		BlockPos pos = context.absolutePos(relativePos);
		context.getLevel().setBlock(pos, DebugContent.DEBUG_CONTAINER.defaultBlockState(), Block.UPDATE_ALL);
		DebugContainerBlockEntity blockEntity = context.getBlockEntity(relativePos, DebugContainerBlockEntity.class);
		blockEntity.setItem(0, new ItemStack(Items.DIAMOND, 4));
		context.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		context.assertItemEntityPresent(Items.DIAMOND, relativePos, 2.0);
		context.succeed();
	}

	@GameTest
	public void menuQuickMoveTransfersMachineStack(GameTestHelper context) {
		Player player = context.makeMockPlayer(GameType.SURVIVAL);
		Inventory inventory = player.getInventory();
		DebugContainerMenu menu = new DebugContainerMenu(0, inventory, BlockPos.ZERO);
		menu.getSlot(0).set(new ItemStack(Items.DIAMOND, 5));
		ItemStack moved = menu.quickMoveStack(player, 0);
		int inventoryCount = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(Items.DIAMOND)) {
				inventoryCount += inventory.getItem(slot).getCount();
			}
		}
		context.assertTrue(moved.getCount() == 5, "quick move returns the original stack");
		context.assertTrue(inventoryCount == 5, "quick move transfers the full stack to player inventory");
		context.succeed();
	}

	@GameTest
	public void chunkDebugAttachmentIsPersistent(GameTestHelper context) {
		LevelChunk chunk = context.getLevel().getChunkAt(BlockPos.ZERO);
		chunk.setAttached(ModAttachments.DEBUG_CHUNK_MARKER, 12);
		context.assertTrue(chunk.getAttachedOrCreate(ModAttachments.DEBUG_CHUNK_MARKER) == 12, "chunk attachment round trip");
		context.assertTrue(ModAttachments.DEBUG_CHUNK_MARKER.isPersistent(), "chunk attachment is persistent");
		context.succeed();
	}

	@Override
	public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
		method.invoke(this, context);
	}
}
