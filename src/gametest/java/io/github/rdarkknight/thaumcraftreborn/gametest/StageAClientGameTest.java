package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.client.config.ClientConfig;
import io.github.rdarkknight.thaumcraftreborn.client.fx.DebugBurstHandler;
import io.github.rdarkknight.thaumcraftreborn.client.screen.DebugContainerScreen;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugData;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.config.CommonConfig;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxDispatcher;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;

public final class StageAClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave savedWorld;
		BlockPos persistedContainerPos;
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft ->
					DebugData.ENTRIES.clientEntries().containsKey(
							ThaumcraftRebornApi.id("sample")
					), 1200);
			context.waitFor(minecraft ->
					ThaumcraftConfig.hasSynchronizedCommon()
							&& ThaumcraftConfig.common(true).equals(ThaumcraftConfig.common(false)), 1200);

			ClientConfig clientDefaults = ClientConfig.CODEC.parse(
					JsonOps.INSTANCE,
					JsonParser.parseString("{}")
			).getOrThrow();
			if (!clientDefaults.equals(ClientConfig.DEFAULT)) {
				throw new AssertionError("empty client config uses defaults");
			}

			int syncsBeforeReload = DebugData.ENTRIES.clientSyncCount();
			singleplayer.getServer().runCommand("reload");
			context.waitFor(minecraft -> DebugData.ENTRIES.clientSyncCount() > syncsBeforeReload, 1200);
			context.waitTicks(10);
			if (DebugData.ENTRIES.clientSyncCount() != syncsBeforeReload + 1
					|| !DebugData.ENTRIES.clientEntries().containsKey(ThaumcraftRebornApi.id("sample"))) {
				throw new AssertionError("data loader syncs exactly once after /reload and retains sample");
			}

			persistedContainerPos = context.computeOnClient(minecraft ->
					minecraft.player.blockPosition().relative(minecraft.player.getDirection(), 3)
			);
			savedWorld = singleplayer.getWorldSave();
			DebugBurstHandler.resetExecutionCount();
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var level = player.level();
				level.setBlock(persistedContainerPos, DebugContent.DEBUG_CONTAINER.defaultBlockState(), 3);
				if (!(level.getBlockEntity(persistedContainerPos) instanceof DebugContainerBlockEntity blockEntity)) {
					throw new AssertionError("debug container block entity is present");
				}
				blockEntity.setItem(0, new ItemStack(Items.DIAMOND, 3));
				level.getChunkAt(persistedContainerPos).setAttached(ModAttachments.DEBUG_CHUNK_MARKER, 42);
				player.openMenu(blockEntity);
				FxDispatcher.send(level, player.position(), DebugFx.DEBUG_BURST, 12);
			});

			context.waitFor(minecraft ->
					minecraft.player != null
							&& minecraft.gui.screen() instanceof DebugContainerScreen
							&& minecraft.player.containerMenu instanceof DebugContainerMenu menu
							&& menu.ticks() > 0, 1200);
			context.waitFor(minecraft -> DebugBurstHandler.executionCount() > 0, 1200);
			context.waitTicks(10);
			context.takeScreenshot("thaumcraft_reborn-stage-a-debug-container");
		}

		try (TestSingleplayerContext reopened = savedWorld.open()) {
			boolean restored = reopened.getServer().computeOnServer(server -> {
				var level = server.overworld();
				LevelChunk chunk = level.getChunkAt(persistedContainerPos);
				if (chunk.getAttachedOrCreate(ModAttachments.DEBUG_CHUNK_MARKER) != 42) {
					return false;
				}
				if (!(level.getBlockEntity(persistedContainerPos) instanceof DebugContainerBlockEntity blockEntity)) {
					return false;
				}
				return blockEntity.getItem(0).is(Items.DIAMOND)
						&& blockEntity.getItem(0).getCount() == 3
						&& blockEntity.ticks() > 0;
			});
			if (!restored) {
				throw new AssertionError("chunk marker, container item, and ticks survive saving and reopening the world");
			}
		}
	}
}
