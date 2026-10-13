package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.content.SalisMundusItem;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.SalisMundusRecipe;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StageDDustGameTests {
	@GameTest
	public void salisRecipeNeedsThreeDistinctAspectsAndReturnsBowlAndFlint(GameTestHelper context) {
		SalisMundusRecipe recipe = new SalisMundusRecipe();
		CraftingInput input = CraftingInput.of(3, 2, List.of(
				new ItemStack(Items.BOWL),
				new ItemStack(Items.FLINT),
				new ItemStack(Items.REDSTONE),
				essence("aer"),
				essence("terra"),
				essence("ignis")
		));
		context.assertTrue(recipe.matches(input, context.getLevel()), "3×2 input with distinct primal crystals crafts Salis Mundus");
		var remaining = recipe.getRemainingItems(input);
		context.assertTrue(remaining.stream().filter(stack -> stack.is(Items.BOWL)).count() == 1
						&& remaining.stream().filter(stack -> stack.is(Items.FLINT)).count() == 1,
				"bowl and flint are returned");
		context.assertTrue(remaining.stream().filter(stack -> stack.is(Items.REDSTONE)).count() == 0
						&& remaining.stream().filter(stack -> stack.is(ThaumcraftContent.CRYSTAL_ESSENCE)).count() == 0,
				"redstone and crystal essence are consumed");

		CraftingInput duplicate = CraftingInput.of(3, 2, List.of(
				new ItemStack(Items.BOWL),
				new ItemStack(Items.FLINT),
				new ItemStack(Items.REDSTONE),
				essence("aer"),
				essence("aer"),
				essence("ignis")
		));
		context.assertTrue(!recipe.matches(duplicate, context.getLevel()), "duplicate crystal aspects are rejected");
		CraftingInput twoByTwo = CraftingInput.of(2, 2, List.of(
				new ItemStack(Items.BOWL),
				new ItemStack(Items.FLINT),
				new ItemStack(Items.REDSTONE),
				essence("aer")
		));
		context.assertTrue(!recipe.matches(twoByTwo, context.getLevel()), "2×2 crafting area is too small");
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void dustTriggersRequireResearchAndReplaceAfterFiftyServerTicks(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.getAbilities().instabuild = false;
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		player.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
		context.getLevel().setBlock(pos, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
		SalisMundusItem dust = (SalisMundusItem) ThaumcraftContent.SALIS_MUNDUS;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(dust, 2));
		InteractionResult locked = dust.useOn(useOnContext(player, pos));
		context.assertTrue(locked == InteractionResult.PASS
						&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 2,
				"unmet FIRSTSTEPS gate leaves the dust and crafting table unchanged");

		var firstSteps = ThaumcraftRebornApi.id("firststeps");
		KnowledgeAccess.get().addResearch(player, firstSteps);
		KnowledgeAccess.get().setResearchStage(player, firstSteps, 1);
		InteractionResult accepted = dust.useOn(useOnContext(player, pos));
		context.assertTrue(accepted == InteractionResult.SUCCESS
						&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
				"known FIRSTSTEPS@1 consumes one Salis Mundus");
		KnowledgeAccess.get().removeResearch(player, firstSteps);
		context.runAfterDelay(49, () -> {
			context.assertTrue(context.getLevel().getBlockState(pos).is(Blocks.CRAFTING_TABLE),
					"the source remains until the 50-tick delay expires");
			context.runAfterDelay(2, () -> {
				context.assertTrue(context.getLevel().getBlockState(pos).is(ThaumcraftContent.ARCANE_WORKBENCH),
						"crafting table transforms after 50 server ticks");
				context.assertTrue(CraftEventRecorder.count(player, ThaumcraftRebornApi.id("arcane_workbench")) == 1,
						"the dust result is recorded as a crafting event");
				context.succeed();
			});
		});
	}

	@GameTest
	public void dustTriggerRejectsWrongBlockAndSneakingAndCreativeDoesNotConsume(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		SalisMundusItem dust = (SalisMundusItem) ThaumcraftContent.SALIS_MUNDUS;
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		context.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		var firstSteps = ThaumcraftRebornApi.id("firststeps");
		KnowledgeAccess.get().addResearch(player, firstSteps);
		KnowledgeAccess.get().setResearchStage(player, firstSteps, 1);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(dust, 3));

		InteractionResult wrongBlock = dust.useOn(useOnContext(player, pos));
		player.setShiftKeyDown(true);
		InteractionResult sneaking = dust.useOn(useOnContext(player, pos));
		player.setShiftKeyDown(false);
		context.assertTrue(wrongBlock == InteractionResult.PASS && sneaking == InteractionResult.PASS
						&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 3,
				"wrong blocks and sneaking do not consume dust");

		context.getLevel().setBlock(pos, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
		player.getAbilities().instabuild = true;
		InteractionResult creative = dust.useOn(useOnContext(player, pos));
		context.assertTrue(creative == InteractionResult.SUCCESS
						&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 3,
				"creative mode does not consume Salis Mundus");
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void bookshelfFlagAndAlchemyResearchUnlockTheirDustTriggers(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.getAbilities().instabuild = false;
		SalisMundusItem dust = (SalisMundusItem) ThaumcraftContent.SALIS_MUNDUS;
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		player.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
		context.getLevel().setBlock(pos, Blocks.BOOKSHELF.defaultBlockState(), 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(dust, 2));
		InteractionResult lockedBookshelf = dust.useOn(useOnContext(player, pos));
		context.assertTrue(lockedBookshelf == InteractionResult.PASS
						&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 2,
				"bookshelf trigger requires the got_dream flag");
		KnowledgeAccess.get().addResearch(player, ThaumcraftRebornApi.id("flag/got_dream"));
		context.assertTrue(dust.useOn(useOnContext(player, pos)) == InteractionResult.SUCCESS
						&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("thaumonomicon_normal")) == 1,
				"unlocked bookshelf trigger records the Thaumonomicon result");

		player.setPos(pos.getX() + 8.0, pos.getY() + 1.0, pos.getZ() + 8.0);
		context.runAfterDelay(51, () -> {
			context.assertTrue(context.getLevel().getBlockState(pos).isAir(),
					"the bookshelf is removed when its delayed transformation produces an item");
			var bookDrops = context.getLevel().getEntitiesOfClass(ItemEntity.class,
					new AABB(pos.getX() - 1, pos.getY() - 2, pos.getZ() - 1,
							pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2))
					.stream().filter(item -> item.getItem().is(ThaumcraftContent.THAUMONOMICON)).toList();
			context.assertTrue(bookDrops.size() == 1 && bookDrops.getFirst().getItem().getCount() == 1,
					"exactly one Thaumonomicon is spawned");
			context.assertTrue(dust.useOn(useOnContext(player, pos)) == InteractionResult.PASS
							&& player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
					"the empty position cannot trigger a second bookshelf transformation");

			player.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
			context.getLevel().setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			InteractionResult lockedCauldron = dust.useOn(useOnContext(player, pos));
			context.assertTrue(lockedCauldron == InteractionResult.PASS,
					"cauldron trigger requires UNLOCKALCHEMY@1");
			var alchemy = ThaumcraftRebornApi.id("unlockalchemy");
			KnowledgeAccess.get().addResearch(player, alchemy);
			KnowledgeAccess.get().setResearchStage(player, alchemy, 1);
			context.assertTrue(dust.useOn(useOnContext(player, pos)) == InteractionResult.SUCCESS
							&& CraftEventRecorder.count(player, ThaumcraftRebornApi.id("crucible")) == 1,
					"unlocked cauldron trigger records the Crucible result");
			context.succeed();
		});
	}

	private static ItemStack essence(String aspectId) {
		Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id(aspectId));
		ItemStack stack = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
		stack.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
		return stack;
	}

	private static UseOnContext useOnContext(ServerPlayer player, BlockPos pos) {
		return new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
	}
}
