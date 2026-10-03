package io.github.rdarkknight.thaumcraftreborn.gametest;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.rdarkknight.thaumcraftreborn.compat.trinkets.TrinketsAccessoryAccess;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.core.accessory.AccessoryAccess;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.component.ProbeStamp;
import io.github.rdarkknight.thaumcraftreborn.core.network.ProbePingPayload;
import io.github.rdarkknight.thaumcraftreborn.core.network.ProbePongPayload;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;

public final class PhaseOneGameTests implements CustomTestMethodInvoker {
	@GameTest
	public void registryIdsUseModNamespace(GameTestHelper context) {
		context.assertTrue(isModId(BuiltInRegistries.ITEM.getKey(DebugContent.TEST_PROBE)), "Probe item namespace");
		context.assertTrue(isModId(BuiltInRegistries.BLOCK.getKey(DebugContent.TEST_RENDER_BLOCK)), "Render block namespace");
		context.assertTrue(isModId(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(DebugContent.TEST_RENDER_BLOCK_ENTITY)), "Block entity namespace");
		context.assertTrue(isModId(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(ModDataComponents.PROBE_STAMP)), "Component namespace");
		context.assertTrue(isModId(ModAttachments.PROBE_INTERACTIONS.identifier()), "Attachment namespace");
		context.assertTrue(isModId(ProbePingPayload.TYPE.id()), "Ping payload namespace");
		context.assertTrue(isModId(ProbePongPayload.TYPE.id()), "Pong payload namespace");
		context.succeed();
	}

	@GameTest
	public void componentCodecsRoundTrip(GameTestHelper context) {
		ProbeStamp original = new ProbeStamp(17, "TestPlayer");
		var encodedStamp = ProbeStamp.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		context.assertTrue(ProbeStamp.CODEC.parse(JsonOps.INSTANCE, encodedStamp).getOrThrow().equals(original), "JSON codec round trip");

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.getLevel().registryAccess());
		ProbeStamp.STREAM_CODEC.encode(buffer, original);
		context.assertTrue(ProbeStamp.STREAM_CODEC.decode(buffer).equals(original), "Stream codec round trip");
		buffer.release();

		ItemStack stack = new ItemStack(DebugContent.TEST_PROBE);
		stack.set(ModDataComponents.PROBE_STAMP, original);
		var ops = RegistryOps.create(JsonOps.INSTANCE, context.getLevel().registryAccess());
		var encodedStack = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
		ItemStack decodedStack = ItemStack.CODEC.parse(ops, encodedStack).getOrThrow();
		context.assertTrue(original.equals(decodedStack.get(ModDataComponents.PROBE_STAMP)), "ItemStack codec preserves component");
		context.succeed();
	}

	@GameTest
	public void attachmentPersistsAcrossEntityReload(GameTestHelper context) {
		EntityType<?> zombieType = BuiltInRegistries.ENTITY_TYPE.get(Identifier.withDefaultNamespace("zombie")).orElseThrow().value();
		Entity original = zombieType.create(context.getLevel(), EntitySpawnReason.TRIGGERED);
		context.assertTrue(original != null, "Zombie entity created");
		original.setAttached(ModAttachments.PROBE_INTERACTIONS, 23);
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.getLevel().registryAccess());
		original.saveWithoutId(output);
		CompoundTag saved = output.buildResult();
		Entity reloaded = zombieType.create(context.getLevel(), EntitySpawnReason.TRIGGERED);
		context.assertTrue(reloaded != null, "Reload entity created");
		reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, context.getLevel().registryAccess(), saved));
		context.assertTrue(reloaded.getAttachedOrCreate(ModAttachments.PROBE_INTERACTIONS) == 23, "Attachment persisted through entity save/load");
		context.succeed();
	}

	@GameTest
	public void trinketsFacadeReadsEquippedSlots(GameTestHelper context) {
		Player equippedPlayer = context.makeMockPlayer(GameType.SURVIVAL);
		TrinketAttachment attachment = TrinketsApi.getAttachment(equippedPlayer);
		TrinketInventory inventory = attachment.getInventory().get("chest").get("necklace");
		inventory.setItem(0, new ItemStack(DebugContent.TEST_PROBE));

		context.assertTrue(AccessoryAccess.get() instanceof TrinketsAccessoryAccess, "Trinkets adapter installed");
		context.assertTrue(AccessoryAccess.get().isEquipped(equippedPlayer, DebugContent.TEST_PROBE), "Probe equipped");
		context.assertTrue(AccessoryAccess.get().getEquipped(equippedPlayer, stack -> stack.is(DebugContent.TEST_PROBE)).size() == 1, "Facade lists one probe");
		AtomicInteger visits = new AtomicInteger();
		AccessoryAccess.get().forEachEquipped(equippedPlayer, stack -> visits.incrementAndGet());
		context.assertTrue(visits.get() == 1, "Facade visits equipped probe once");

		Player emptyPlayer = context.makeMockPlayer(GameType.SURVIVAL);
		context.assertTrue(!AccessoryAccess.get().isEquipped(emptyPlayer, DebugContent.TEST_PROBE), "Empty player has no probe");
		context.assertTrue(AccessoryAccess.get().getEquipped(emptyPlayer, stack -> true).isEmpty(), "Empty player has no equipped stacks");
		context.succeed();
	}

	@Override
	public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
		method.invoke(this, context);
	}

	private static boolean isModId(net.minecraft.resources.Identifier id) {
		return id != null && id.getNamespace().equals("thaumcraft_reborn");
	}
}
