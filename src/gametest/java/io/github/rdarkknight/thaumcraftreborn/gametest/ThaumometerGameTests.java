package io.github.rdarkknight.thaumcraftreborn.gametest;

import eu.pb4.trinkets.api.TrinketsApi;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ScanningAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.systems.PickupResearchFlags;
import io.github.rdarkknight.thaumcraftreborn.systems.RevealingEquipment;
import io.github.rdarkknight.thaumcraftreborn.systems.ThaumometerSystem;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ThaumometerGameTests {
	@GameTest
	public void blockItemAndEntityScansGrantKeysAndObservationOnce(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		var pos = new BlockPos(2, 2, 2);
		context.getLevel().setBlock(pos, ThaumcraftContent.ORE_AMBER.defaultBlockState(), 3);
		int blockObservation = observation(player);
		context.assertTrue(ScanningAccess.get().scanBlock(player, pos) == ScanningAccess.ScanResult.SCANNED,
				"amber ore has a special scan key");
		context.assertTrue(KnowledgeAccess.get().knowledge(player).isResearchKnown(ThaumcraftRebornApi.id("scan/ore_amber")),
				"amber ore scan key is stored");
		int afterBlockObservation = observation(player);
		context.assertTrue(afterBlockObservation > blockObservation, "block scan grants OBSERVATION");
		context.assertTrue(ScanningAccess.get().scanBlock(player, pos) == ScanningAccess.ScanResult.ALREADY_KNOWN
						&& observation(player) == afterBlockObservation,
				"rescanning a block is a no-op");

		int itemObservation = observation(player);
		ScanningAccess.ScanResult itemResult = ScanningAccess.get().scanItem(player, new ItemStack(ThaumcraftContent.AMBER));
		context.assertTrue(itemResult == ScanningAccess.ScanResult.SCANNED, "aspect-backed items can be scanned");
		var knowledge = KnowledgeAccess.get().knowledge(player);
		context.assertTrue(knowledge.isResearchKnown(ThaumcraftRebornApi.id("scan/amber"))
						&& knowledge.isResearchKnown(ThaumcraftRebornApi.id("scan/aspect/vitreus")),
				"item and each discovered aspect use converted scan keys");
		int observation = observation(player);
		context.assertTrue(observation > itemObservation, "item scan grants OBSERVATION");
		context.assertTrue(ScanningAccess.get().scanItem(player, new ItemStack(ThaumcraftContent.AMBER))
						== ScanningAccess.ScanResult.ALREADY_KNOWN
						&& observation(player) == observation,
				"rescanning known item and aspects grants no additional observation");

		var cow = context.spawn(
				BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "cow")),
				new BlockPos(4, 2, 2)
		);
		int entityObservation = observation(player);
		context.assertTrue(ScanningAccess.get().scanEntity(player, cow) == ScanningAccess.ScanResult.SCANNED,
				"aspect-backed vanilla entities can be scanned");
		context.assertTrue(KnowledgeAccess.get().knowledge(player).isResearchKnown(ThaumcraftRebornApi.id("scan/cow")),
				"entity scan key is stored");
		int afterEntityObservation = observation(player);
		context.assertTrue(afterEntityObservation > entityObservation, "entity scan grants OBSERVATION");
		context.assertTrue(ScanningAccess.get().scanEntity(player, cow) == ScanningAccess.ScanResult.ALREADY_KNOWN
						&& observation(player) == afterEntityObservation,
				"rescanning an entity is a no-op");
		context.succeed();
	}

	@GameTest
	public void unscannableItemsAreNoOps(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		int observations = observation(player);
		context.assertTrue(ScanningAccess.get().scanItem(player, ItemStack.EMPTY) == ScanningAccess.ScanResult.NOT_SCANNABLE,
				"empty stacks are unscannable");
		context.assertTrue(observation(player) == observations,
				"unscannable items do not grant observation");
		context.succeed();
	}

	@GameTest
	public void revealingPickupFlagsAndAuraEligibility(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		context.assertTrue(!RevealingEquipment.isRevealing(player), "players without revealing gear do not reveal");
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ThaumcraftContent.GOGGLES));
		context.assertTrue(RevealingEquipment.isRevealing(player), "helmet goggles enable revealing");
		player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);

		PickupResearchFlags.onPickup(player, new ItemStack(ThaumcraftContent.THAUMONOMICON));
		PickupResearchFlags.onPickup(player, new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE));
		var knowledge = KnowledgeAccess.get().knowledge(player);
		context.assertTrue(knowledge.isResearchKnown(ThaumcraftRebornApi.id("flag/got_thaumonomicon"))
						&& knowledge.isResearchKnown(ThaumcraftRebornApi.id("flag/got_crystals")),
				"pickup grants Thaumonomicon and crystal essence flags");
		PickupResearchFlags.onWakeup(player);
		context.assertTrue(knowledge.isResearchKnown(ThaumcraftRebornApi.id("flag/got_dream")),
				"waking after acquiring crystal essence grants the dream flag");

		context.assertTrue(!ThaumometerSystem.shouldSyncAura(player), "aura is not sent without a thaumometer");
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
				new ItemStack(ThaumcraftContent.THAUMOMETER));
		context.assertTrue(ThaumometerSystem.shouldSyncAura(player), "aura is sent when the thaumometer is selected");
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.getInventory().setItem(0, new ItemStack(ThaumcraftContent.THAUMOMETER));
		context.assertTrue(ThaumometerSystem.shouldSyncAura(player), "aura is sent when the thaumometer is held or in slot zero");
		player.getInventory().setItem(0, new ItemStack(Items.STICK));
		context.assertTrue(!ThaumometerSystem.shouldSyncAura(player), "aura condition stops when the thaumometer leaves slot zero");
		context.succeed();
	}

	@GameTest
	public void gogglesRevealFromTrinketSlotWithoutArmorModifiers(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ItemStack goggles = new ItemStack(ThaumcraftContent.GOGGLES);
		var faceInventory = TrinketsApi.getAttachment(player).getInventory().get("head").get("face");
		var faceSlot = faceInventory.getSlotAccess(0);

		context.assertTrue(faceSlot.slotType().validatorCheck(goggles, faceSlot, player),
				"head/face accepts goggles");
		context.assertTrue(!faceSlot.slotType().validatorCheck(new ItemStack(Items.STICK), faceSlot, player),
				"head/face rejects a stick");
		context.assertTrue(!RevealingEquipment.isRevealing(player), "empty slots do not reveal");

		player.setItemSlot(EquipmentSlot.HEAD, goggles.copy());
		context.assertTrue(RevealingEquipment.isRevealing(player), "helmet goggles enable revealing");
		player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);

		double armorBefore = player.getAttributeValue(Attributes.ARMOR);
		faceInventory.setItem(0, goggles);
		context.assertTrue(RevealingEquipment.isRevealing(player), "head/face goggles enable revealing");
		context.assertTrue(armorBefore == 0.0 && player.getAttributeValue(Attributes.ARMOR) == armorBefore,
				"head/face goggles do not grant armor");
		faceInventory.setItem(0, ItemStack.EMPTY);
		context.assertTrue(!RevealingEquipment.isRevealing(player), "removing trinket goggles disables revealing");
		context.succeed();
	}

	@GameTest
	public void pickupFlagsFollowVanillaDelayAndOwnerGates(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ItemEntity itemEntity = new ItemEntity(
				context.getLevel(),
				player.getX(),
				player.getY(),
				player.getZ(),
				new ItemStack(ThaumcraftContent.THAUMONOMICON)
		);
		itemEntity.setPickUpDelay(10);
		context.getLevel().addFreshEntity(itemEntity);
		itemEntity.playerTouch(player);

		var knowledge = KnowledgeAccess.get().knowledge(player);
		var flag = ThaumcraftRebornApi.id("flag/got_thaumonomicon");
		context.assertTrue(!knowledge.isResearchKnown(flag), "pickup delay prevents pickup flags");

		itemEntity.setNoPickUpDelay();
		itemEntity.setTarget(UUID.randomUUID());
		itemEntity.playerTouch(player);
		context.assertTrue(!knowledge.isResearchKnown(flag), "another player's targeted item grants no pickup flag");

		itemEntity.setTarget(player.getUUID());
		itemEntity.playerTouch(player);
		context.assertTrue(knowledge.isResearchKnown(flag), "eligible pickup grants its flag before inventory insertion");
		context.succeed();
	}

	private static int observation(ServerPlayer player) {
		var knowledge = KnowledgeAccess.get().knowledge(player);
		return ResearchAccess.get().categories(false).keySet().stream()
				.mapToInt(category -> knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION, Optional.of(category)))
				.sum();
	}
}
