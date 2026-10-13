package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchMenu;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class StageDWorkbenchGameTests {
	@GameTest
	public void thaumometerArcaneCraftDrainsTwentyVisAndConsumesInputs(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 100.0F, true);
		ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		context.assertTrue(menu.getSlot(15).getItem().is(ThaumcraftContent.THAUMOMETER),
				"known FIRSTSTEPS@2 and six matching crystal essences show the Thaumometer");
		ItemStack correct = essence("aer");
		ItemStack wrong = essence("terra");
		context.assertTrue(menu.getSlot(9).mayPlace(correct) && !menu.getSlot(9).mayPlace(wrong),
				"the aer crystal slot rejects other primal aspects");
		ItemStack crafted = menu.getSlot(15).safeTake(1, 1, player);
		context.assertTrue(crafted.is(ThaumcraftContent.THAUMOMETER)
						&& AuraAccess.get().getVis(player.level(), blockEntity.getBlockPos()) == 80.0F,
				"taking the result drains the exact 20 vis");
		context.assertTrue(blockEntity.getItem(1).isEmpty() && blockEntity.getItem(3).isEmpty()
						&& blockEntity.getItem(4).isEmpty() && blockEntity.getItem(5).isEmpty()
						&& blockEntity.getItem(7).isEmpty()
						&& java.util.stream.IntStream.range(9, 15).allMatch(slot -> blockEntity.getItem(slot).isEmpty())
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("thaumometer")) == 1,
				"all ingredients and six crystals are consumed and the craft is recorded");
		context.succeed();
	}

	@GameTest
	public void shiftClickArcaneResultTransfersItAndPaysExactlyOnce(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 100.0F, true);
		ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		ItemStack crafted = menu.quickMoveStack(player, 15);
		context.assertTrue(crafted.is(ThaumcraftContent.THAUMOMETER)
						&& player.getInventory().contains(new ItemStack(ThaumcraftContent.THAUMOMETER))
						&& AuraAccess.get().getVis(player.level(), blockEntity.getBlockPos()) == 80.0F,
				"shift-click moves the result and drains 20 vis once");
		context.assertTrue(blockEntity.getItem(1).isEmpty() && blockEntity.getItem(3).isEmpty()
						&& blockEntity.getItem(4).isEmpty() && blockEntity.getItem(5).isEmpty()
						&& blockEntity.getItem(7).isEmpty()
						&& java.util.stream.IntStream.range(9, 15).allMatch(slot -> blockEntity.getItem(slot).isEmpty())
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("thaumometer")) == 1,
				"shift-click consumes the recipe once and records one craft");
		context.succeed();
	}

	@GameTest
	public void visLossAfterPreviewReturnsTheDeliveredResultWithoutPayment(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 100.0F, true);
		ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		Slot output = menu.getSlot(15);
		context.assertTrue(output.getItem().is(ThaumcraftContent.THAUMOMETER) && output.mayPickup(player),
				"the funded preview is initially available");

		context.getLevel().getChunkAt(blockEntity.getBlockPos()).setAttached(
				ModAttachments.AURA, new AuraChunkData((short) 100, 19.0F, 0.0F));
		ItemStack delivered = output.remove(1);
		player.containerMenu.setCarried(delivered.copy());
		output.onTake(player, delivered);
		context.assertTrue(player.containerMenu.getCarried().isEmpty()
						&& player.getInventory().getItem(0).isEmpty()
						&& output.getItem().isEmpty()
						&& !output.mayPickup(player)
						&& AuraAccess.get().getVis(player.level(), blockEntity.getBlockPos()) == 19.0F,
				"an unaffordable take returns no item and drains no vis");
		context.assertTrue(blockEntity.getItem(1).getCount() == 1 && blockEntity.getItem(3).getCount() == 1
						&& blockEntity.getItem(4).getCount() == 1 && blockEntity.getItem(5).getCount() == 1
						&& blockEntity.getItem(7).getCount() == 1
						&& java.util.stream.IntStream.range(9, 15).allMatch(slot -> blockEntity.getItem(slot).getCount() == 1)
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("thaumometer")) == 0,
				"failed payment consumes neither ingredients nor crystals and records no craft");
		context.succeed();
	}

	@GameTest
	public void gogglesApplyFivePercentVisDiscountAtTheWorkbench(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 100.0F, true);
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ThaumcraftContent.GOGGLES));
		ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		context.assertTrue(menu.getSlot(15).hasItem(), "helmet goggles preserve the Thaumometer preview");
		ItemStack crafted = menu.getSlot(15).safeTake(1, 1, player);
		context.assertTrue(crafted.is(ThaumcraftContent.THAUMOMETER)
						&& AuraAccess.get().getVis(player.level(), blockEntity.getBlockPos()) == 81.0F,
				"the 5% goggles discount truncates 20 vis to 19");
		context.succeed();
	}

	@GameTest
	public void unknownGateFallsBackToVanillaAndInsufficientVisHidesArcaneOutput(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 19.0F, false);
		ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		context.assertTrue(menu.getSlot(15).getItem().isEmpty(),
				"unknown FIRSTSTEPS falls back to vanilla 3×3, which has no Thaumometer recipe");

		var firstSteps = ThaumcraftRebornApi.id("firststeps");
		KnowledgeAccess.get().addResearch(player, firstSteps);
		KnowledgeAccess.get().setResearchStage(player, firstSteps, 2);
		menu.slotsChanged(blockEntity);
		context.assertTrue(menu.getSlot(15).getItem().isEmpty(),
				"known arcane recipe remains hidden when the chunk lacks 20 vis");
		context.succeed();
	}

	@GameTest
	public void closingAndReopeningWorkbenchKeepsItsInputInventory(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		ArcaneWorkbenchBlockEntity blockEntity = prepareWorkbench(context, player, 0.0F, false);
		ItemStack saved = new ItemStack(Items.DIAMOND, 3);
		blockEntity.setItem(0, saved);
		ArcaneWorkbenchMenu first = new ArcaneWorkbenchMenu(1, player.getInventory(), blockEntity.getBlockPos());
		first.removed(player);
		ArcaneWorkbenchMenu reopened = new ArcaneWorkbenchMenu(2, player.getInventory(), blockEntity.getBlockPos());
		context.assertTrue(blockEntity.getItem(0).getCount() == 3 && reopened.getSlot(0).getItem().getCount() == 3
						&& !player.getInventory().contains(new ItemStack(Items.DIAMOND)),
				"closing the menu leaves input stacks in the persistent block entity");
		context.succeed();
	}

	private static ArcaneWorkbenchBlockEntity prepareWorkbench(
			GameTestHelper context,
			ServerPlayer player,
			float vis,
			boolean unlock
	) {
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		context.getLevel().setBlock(pos, ThaumcraftContent.ARCANE_WORKBENCH.defaultBlockState(), 3);
		ArcaneWorkbenchBlockEntity blockEntity =
				(ArcaneWorkbenchBlockEntity) context.getLevel().getBlockEntity(pos);
		context.getLevel().getChunkAt(pos).setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, vis, 0.0F));
		if (unlock) {
			var firstSteps = ThaumcraftRebornApi.id("firststeps");
			KnowledgeAccess.get().addResearch(player, firstSteps);
			KnowledgeAccess.get().setResearchStage(player, firstSteps, 2);
		}
		blockEntity.setItem(1, new ItemStack(Items.GOLD_INGOT));
		blockEntity.setItem(3, new ItemStack(Items.GOLD_INGOT));
		blockEntity.setItem(4, new ItemStack(Items.GLASS));
		blockEntity.setItem(5, new ItemStack(Items.GOLD_INGOT));
		blockEntity.setItem(7, new ItemStack(Items.GOLD_INGOT));
		List<String> primal = List.of("aer", "terra", "ignis", "aqua", "ordo", "perditio");
		for (int slot = 0; slot < primal.size(); slot++) {
			blockEntity.setItem(slot + 9, essence(primal.get(slot)));
		}
		return blockEntity;
	}

	private static ItemStack essence(String aspectId) {
		Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id(aspectId));
		ItemStack stack = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
		stack.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
		return stack;
	}
}
