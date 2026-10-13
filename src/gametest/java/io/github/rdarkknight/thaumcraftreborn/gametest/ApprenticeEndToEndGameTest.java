package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ScanningAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftAccess;
import io.github.rdarkknight.thaumcraftreborn.content.SalisMundusItem;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.crucible.CrucibleBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableMenu;
import io.github.rdarkknight.thaumcraftreborn.content.research.ScribingToolsItem;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchMenu;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.PickupResearchFlags;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.SalisMundusRecipe;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.TheorycraftSystem;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;

public final class ApprenticeEndToEndGameTest {
	private static final net.minecraft.resources.Identifier FIRST_STEPS = ThaumcraftRebornApi.id("firststeps");
	private static final net.minecraft.resources.Identifier UNLOCK_ALCHEMY = ThaumcraftRebornApi.id("unlockalchemy");
	private static final net.minecraft.resources.Identifier BASE_ALCHEMY = ThaumcraftRebornApi.id("basealchemy");
	private static final net.minecraft.resources.Identifier METALLURGY = ThaumcraftRebornApi.id("metallurgy");
	private static final net.minecraft.resources.Identifier BASICS = ThaumcraftRebornApi.id("basics");
	private static final net.minecraft.resources.Identifier ALCHEMY = ThaumcraftRebornApi.id("alchemy");

	@GameTest(maxTicks = 900)
	public void apprenticeFollowsGameplayActionsThroughThaumium(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.getAbilities().instabuild = false;
		BlockPos base = context.absolutePos(new BlockPos(1, 1, 1));
		player.setPos(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);

		ItemEntity crystal = new ItemEntity(context.getLevel(), player.getX(), player.getY(), player.getZ(),
				essence("aer"));
		context.getLevel().addFreshEntity(crystal);
		crystal.setNoPickUpDelay();
		crystal.playerTouch(player);
		PickupResearchFlags.onWakeup(player);
		context.assertTrue(KnowledgeAccess.get().knowledge(player)
						.isResearchKnown(ThaumcraftRebornApi.id("flag/got_crystals"))
						&& KnowledgeAccess.get().knowledge(player).isResearchKnown(ThaumcraftRebornApi.id("flag/got_dream")),
				"pickup and waking grant the crystal and dream flags");
		CraftingInput salisInput = CraftingInput.of(3, 2, List.of(
				new ItemStack(Items.BOWL), new ItemStack(Items.FLINT), new ItemStack(Items.REDSTONE),
				essence("aer"), essence("terra"), essence("ignis")));
		var salisRecipe = context.getLevel().getServer().getRecipeManager()
				.getRecipeFor(RecipeType.CRAFTING, salisInput, context.getLevel()).orElseThrow();
		context.assertTrue(salisRecipe.value() instanceof SalisMundusRecipe,
				"vanilla materials and three primal crystals match the Salis Mundus recipe");
		ItemStack salis = salisRecipe.value().assemble(salisInput);

		BlockPos bookshelf = base.offset(2, 0, 0);
		context.getLevel().setBlock(bookshelf, Blocks.BOOKSHELF.defaultBlockState(), 3);
		useSalis(player, salis, bookshelf);
		context.runAfterDelay(51, () -> {
			context.assertTrue(context.getLevel().getBlockState(bookshelf).isAir(),
					"the bookshelf transformation produces the Thaumonomicon item");
			List<ItemEntity> books = context.getLevel().getEntitiesOfClass(ItemEntity.class,
					new net.minecraft.world.phys.AABB(bookshelf).inflate(2.0),
					entity -> entity.getItem().is(ThaumcraftContent.THAUMONOMICON));
			context.assertTrue(books.size() == 1, "the bookshelf yields exactly one Thaumonomicon");
			books.getFirst().playerTouch(player);
			context.assertTrue(KnowledgeAccess.get().knowledge(player)
							.isResearchKnown(ThaumcraftRebornApi.id("flag/got_thaumonomicon")),
					"picking up the Thaumonomicon grants its flag");
			context.assertTrue(ResearchProgression.progressResearch(player, FIRST_STEPS, true),
					"the Thaumonomicon research action opens First Steps");

			BlockPos tablePos = base;
			context.getLevel().setBlock(tablePos, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
			useSalis(player, salis.copy(), tablePos);
			context.runAfterDelay(51, () -> {
				context.assertTrue(context.getLevel().getBlockEntity(tablePos) instanceof ArcaneWorkbenchBlockEntity,
						"Salis Mundus transforms the crafting table into the Arcane Workbench");
				context.assertTrue(ResearchProgression.progressStage(player, FIRST_STEPS),
						"crafting the Workbench advances First Steps through its real requirement");

				ArcaneWorkbenchBlockEntity workbench = (ArcaneWorkbenchBlockEntity) context.getLevel().getBlockEntity(tablePos);
				context.getLevel().getChunkAt(tablePos).setAttached(
						io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments.AURA,
						new AuraChunkData((short) 100, 100.0F, 0.0F));
				workbench.setItem(1, new ItemStack(Items.GOLD_INGOT));
				workbench.setItem(3, new ItemStack(Items.GOLD_INGOT));
				workbench.setItem(4, new ItemStack(Items.GLASS));
				workbench.setItem(5, new ItemStack(Items.GOLD_INGOT));
				workbench.setItem(7, new ItemStack(Items.GOLD_INGOT));
				String[] primals = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};
				for (int index = 0; index < primals.length; index++) {
					workbench.setItem(9 + index, essence(primals[index]));
				}
				ArcaneWorkbenchMenu workbenchMenu = new ArcaneWorkbenchMenu(1, player.getInventory(), tablePos);
				ItemStack thaumometer = workbenchMenu.getSlot(15).safeTake(1, 1, player);
				context.assertTrue(thaumometer.is(ThaumcraftContent.THAUMOMETER),
						"the real Workbench result slot crafts the gated Thaumometer");

				player.setItemInHand(InteractionHand.MAIN_HAND, thaumometer);
				for (var raw : List.of(Items.BLAZE_POWDER, Items.GUNPOWDER, Items.SLIME_BALL, Items.GLOWSTONE,
						Items.GLOWSTONE_DUST, Items.GOLD_INGOT, Items.IRON_INGOT, Items.GLASS, Items.PAPER,
						Items.FEATHER, Items.REDSTONE, Items.CLAY_BALL, Items.BONE, Items.STRING, Items.LEATHER,
						Items.STICK, Items.COAL, Items.FLINT, Items.WHEAT, Items.SPIDER_EYE, Items.SUGAR,
						Items.QUARTZ, Items.SAND, Items.COBBLESTONE, Items.OAK_PLANKS, Items.OAK_SLAB,
						Items.BUCKET, Items.WATER_BUCKET, Items.TORCH, Items.OAK_LOG, Items.BOWL,
						Items.ROTTEN_FLESH, Items.DIAMOND, Items.COPPER_INGOT, Items.ENDER_PEARL,
						Items.EGG, Items.RAW_COPPER, Items.RAW_IRON, Items.RAW_GOLD)) {
					ScanningAccess.get().scanItem(player, new ItemStack(raw));
				}
				context.assertTrue(ResearchProgression.progressStage(player, FIRST_STEPS)
								&& ResearchProgression.progressStage(player, FIRST_STEPS),
						"the Thaumometer scans supply First Steps observation and its crafted-item flag");
				context.assertTrue(KnowledgeAccess.get().knowledge(player)
								.getKnowledgeRaw(KnowledgeType.OBSERVATION, Optional.of(ALCHEMY))
								>= KnowledgeType.OBSERVATION.progression() * 2,
						"raw scans provide both Alchemy observations required later");
				context.assertTrue(ResearchProgression.progressResearch(player, UNLOCK_ALCHEMY, true)
								&& ResearchProgression.progressStage(player, UNLOCK_ALCHEMY),
						"scanned observations unlock Alchemy and pay its first stage");

				BlockPos cauldron = base.offset(0, 0, 2);
				context.getLevel().setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 3);
				useSalis(player, salis.copy(), cauldron);
				context.runAfterDelay(51, () -> {
					context.assertTrue(context.getLevel().getBlockEntity(cauldron) instanceof CrucibleBlockEntity,
							"Salis Mundus creates the Crucible BlockEntity");
					context.assertTrue(ResearchProgression.progressStage(player, UNLOCK_ALCHEMY),
							"the recorded Crucible craft advances Alchemy");
					CrucibleBlockEntity crucible = (CrucibleBlockEntity) context.getLevel().getBlockEntity(cauldron);
					crucible.setWaterMb(1000);
					context.getLevel().setBlock(cauldron.below(), Blocks.LAVA.defaultBlockState(), 3);
					context.runAfterDelay(152, () -> {
						context.assertTrue(crucible.heat() > 150, "lava heats the water-filled Crucible above the strict threshold");
						crucible.processItem(new ItemStack(Items.BLAZE_POWDER, 10), player);
						crucible.processItem(new ItemStack(Items.GLOWSTONE), player);
						ItemEntity glowstone = new ItemEntity(context.getLevel(), cauldron.getX() + 0.5,
								cauldron.getY() + 0.5, cauldron.getZ() + 0.5, new ItemStack(Items.GLOWSTONE_DUST));
						glowstone.setThrower(player);
						context.getLevel().addFreshEntity(glowstone);
						crucible.entityInside(glowstone);
						context.assertTrue(CraftEventRecorder.count(player, ThaumcraftRebornApi.id("nitor_yellow")) > 0,
								"processing the thrown catalyst with dissolved vanilla aspects crafts Nitor");
						context.assertTrue(ResearchProgression.progressStage(player, UNLOCK_ALCHEMY),
								"the Nitor craft completes Unlock Alchemy");
						context.assertTrue(ResearchProgression.progressResearch(player, BASE_ALCHEMY, true)
										&& ResearchProgression.progressResearch(player, METALLURGY, true),
								"completed Alchemy opens Base Alchemy and Metallurgy");

						crucible.processItem(new ItemStack(Items.FLINT), player);
						crucible.processItem(new ItemStack(Items.IRON_INGOT), player);
						context.assertTrue(CraftEventRecorder.count(player, ThaumcraftRebornApi.id("ingot_brass")) > 0,
								"raw flint and iron make Brass through the Crucible");
						context.assertTrue(ResearchProgression.progressStage(player, METALLURGY),
								"the actual Brass craft and remaining observation advance Metallurgy");
						BlockPos table = base.offset(5, 0, 5);
						CraftingInput tableInput = CraftingInput.of(3, 2, List.of(
								new ItemStack(Items.OAK_SLAB), new ItemStack(Items.OAK_SLAB),
								new ItemStack(Items.OAK_SLAB), new ItemStack(Items.OAK_PLANKS),
								ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS)));
						var tableRecipe = context.getLevel().getServer().getRecipeManager()
								.getRecipeFor(RecipeType.CRAFTING, tableInput, context.getLevel()).orElseThrow();
						ItemStack craftedTable = tableRecipe.value().assemble(tableInput);
						context.assertTrue(craftedTable.is(ThaumcraftContent.TABLE_WOOD.asItem()),
								"vanilla planks and slabs craft the wooden research-table block");
						context.getLevel().setBlock(table, ThaumcraftContent.TABLE_WOOD.defaultBlockState(), 3);
						CraftingInput toolsInput = CraftingInput.of(2, 2, List.of(new ItemStack(Items.GLASS_BOTTLE),
								new ItemStack(Items.FEATHER), new ItemStack(Items.INK_SAC), ItemStack.EMPTY));
						var toolsRecipe = context.getLevel().getServer().getRecipeManager()
								.getRecipeFor(RecipeType.CRAFTING, toolsInput, context.getLevel()).orElseThrow();
						ItemStack tools = toolsRecipe.value().assemble(toolsInput);
						context.assertTrue(tools.is(ThaumcraftContent.SCRIBING_TOOLS),
								"the vanilla bottle, feather, and ink sac craft scribing tools");
						player.setItemInHand(InteractionHand.MAIN_HAND, tools);
						((ScribingToolsItem) ThaumcraftContent.SCRIBING_TOOLS).useOn(
								new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND,
										new BlockHitResult(Vec3.atCenterOf(table), Direction.UP, table, false)));
						context.assertTrue(context.getLevel().getBlockEntity(table) instanceof ResearchTableBlockEntity,
								"scribing tools convert the wooden table into a Research Table");
						ResearchTableBlockEntity researchTable =
								(ResearchTableBlockEntity) context.getLevel().getBlockEntity(table);
						CraftingInput paperInput = CraftingInput.of(3, 1, List.of(
								new ItemStack(Items.SUGAR_CANE), new ItemStack(Items.SUGAR_CANE),
								new ItemStack(Items.SUGAR_CANE)));
						var paperRecipe = context.getLevel().getServer().getRecipeManager()
								.getRecipeFor(RecipeType.CRAFTING, paperInput, context.getLevel()).orElseThrow();
						ItemStack craftedPaper = paperRecipe.value().assemble(paperInput);
						for (int sheet = 1; sheet < 43; sheet++) {
							craftedPaper.grow(3);
						}
						craftedPaper.setCount(128);
						context.assertTrue(craftedPaper.is(Items.PAPER),
								"vanilla sugar cane recipes supply the Research Table paper");
						researchTable.setItem(1, craftedPaper);
						ResearchTableMenu researchMenu = new ResearchTableMenu(2, player.getInventory(), table);
						TheorycraftSystem.seedSessionRandomForTesting(player, 0x544341);
						context.runBeforeTestEnd(() -> TheorycraftSystem.clearSessionRandomForTesting(player));
						for (int session = 0; session < 20
								&& KnowledgeAccess.get().knowledge(player).getKnowledge(
										KnowledgeType.THEORY, Optional.of(ALCHEMY)) < 1; session++) {
							context.assertTrue(researchMenu.clickMenuButton(player, ResearchTableMenu.START_SESSION),
									"the real Research Table menu starts a material-backed session");
							for (int draw = 0; draw < 128 && researchTable.data().isActive(); draw++) {
								if (!researchMenu.clickMenuButton(player, ResearchTableMenu.DRAW_TWO)) {
									continue;
								}
								var offers = researchTable.data().offeredCards();
								int selected = 0;
								for (int index = 0; index < offers.size(); index++) {
									var definition = TheorycraftAccess.get().card(offers.get(index).card()).orElseThrow();
									if (definition.category(offers.get(index)).filter(ALCHEMY::equals).isPresent()) {
										selected = index;
										break;
									}
								}
								var chosen = offers.get(selected);
								TheorycraftAccess.get().card(chosen.card()).orElseThrow()
										.requiredItems(chosen).forEach(item -> player.getInventory().add(item.copy()));
								researchMenu.clickMenuButton(player, ResearchTableMenu.PICK_CARD_BASE + selected);
							}
						}
						context.assertTrue(KnowledgeAccess.get().knowledge(player)
										.getKnowledge(KnowledgeType.THEORY, Optional.of(ALCHEMY)) >= 1,
								"seeded Research Table sessions grant actual Alchemy THEORY");

						context.assertTrue(ResearchProgression.progressStage(player, METALLURGY),
								"Alchemy THEORY advances Metallurgy after the Brass stage");
						CraftingInput eyeInput = CraftingInput.of(2, 1,
								List.of(new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.BLAZE_POWDER)));
						var eyeRecipe = context.getLevel().getServer().getRecipeManager()
								.getRecipeFor(RecipeType.CRAFTING, eyeInput, context.getLevel()).orElseThrow();
						ItemStack enderEye = eyeRecipe.value().assemble(eyeInput);
						context.assertTrue(enderEye.is(Items.ENDER_EYE),
								"the vanilla pearl and blaze powder recipe crafts the crucible catalyst");
						crucible.processItem(enderEye, player);
						crucible.processItem(new ItemStack(Items.STONE), player);
						crucible.processItem(new ItemStack(Items.IRON_INGOT), player);
						context.assertTrue(CraftEventRecorder.count(player,
										ThaumcraftRebornApi.id("ingot_thaumium")) > 0
										&& ResearchProgression.progressStage(player, METALLURGY),
								"the Crucible crafts Thaumium and the real research action completes Metallurgy");
						context.succeed();
					});
				});
			});
		});
	}

	private static void useSalis(ServerPlayer player, ItemStack salis, BlockPos pos) {
		player.setItemInHand(InteractionHand.MAIN_HAND, salis);
		((SalisMundusItem) ThaumcraftContent.SALIS_MUNDUS).useOn(new net.minecraft.world.item.context.UseOnContext(
				player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
	}

	private static ItemStack essence(String id) {
		Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id(id));
		ItemStack stack = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
		stack.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
		return stack;
	}
}
