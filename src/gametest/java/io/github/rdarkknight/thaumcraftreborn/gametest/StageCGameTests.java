package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.RecipeGateAccess;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import io.github.rdarkknight.thaumcraftreborn.content.PrimalCrystalBlock;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class StageCGameTests {
	@GameTest
	public void stageCRegistriesToolsDropsAndMappings(GameTestHelper context) {
		context.assertTrue(BuiltInRegistries.ITEM.getValue(ThaumcraftRebornApi.id("amber")) == ThaumcraftContent.AMBER,
				"amber item is registered");
		context.assertTrue(BuiltInRegistries.BLOCK.getValue(ThaumcraftRebornApi.id("ore_cinnabar")) == ThaumcraftContent.ORE_CINNABAR,
				"cinnabar ore is registered");
		context.assertTrue(BuiltInRegistries.ITEM.getValue(ThaumcraftRebornApi.id("crystal_essence")) == ThaumcraftContent.CRYSTAL_ESSENCE,
				"crystal essence is one registered item");
		for (var block : ThaumcraftContent.BLOCKS) {
			context.assertTrue(BuiltInRegistries.BLOCK.getKey(block) != null
							&& BuiltInRegistries.ITEM.getKey(block.asItem()) != null,
					"registered block has a block and item id: " + BuiltInRegistries.BLOCK.getKey(block));
		}
		for (var item : ThaumcraftContent.ITEMS) {
			context.assertTrue(BuiltInRegistries.ITEM.getKey(item) != null,
					"registered content item has an id");
		}
		context.assertTrue(ThaumcraftContent.THAUMIUM_TOOL_MATERIAL.durability() == 500
						&& ThaumcraftContent.THAUMIUM_TOOL_MATERIAL.speed() == 7.0F
						&& ThaumcraftContent.THAUMIUM_TOOL_MATERIAL.attackDamageBonus() == 2.5F
						&& ThaumcraftContent.THAUMIUM_TOOL_MATERIAL.enchantmentValue() == 22,
				"thaumium tool material matches TC6 stats");
		context.assertTrue(ThaumcraftContent.THAUMIUM_TOOL_MATERIAL.repairItems().location()
						.equals(ThaumcraftRebornApi.id("repairs/thaumium_tools")),
				"thaumium tools use the ingot repair tag");
		AspectList quicksilverNuggetAspects = AspectLookup.get().getAspects(
				context.getLevel(), new ItemStack(ThaumcraftContent.NUGGET_QUICKSILVER)
		);
		context.assertTrue(quicksilverNuggetAspects.amount(
						ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("metallum"))
				) == 1,
				"quicksilver nugget uses the TC6 runtime-dumped mapping");

		List<ItemStack> amberDrops = Block.getDrops(
				ThaumcraftContent.ORE_AMBER.defaultBlockState(), context.getLevel(), BlockPos.ZERO, null
		);
		context.assertTrue(!amberDrops.isEmpty() && amberDrops.getFirst().is(ThaumcraftContent.AMBER)
						&& amberDrops.getFirst().getCount() >= 1 && amberDrops.getFirst().getCount() <= 2,
				"amber ore drops one or two amber items");

		Aspect aer = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		Aspect terra = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("terra"));
		Aspect vinculum = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("vinculum"));
		Aspect vitreus = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("vitreus"));
		AspectList amberAspects = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(ThaumcraftContent.AMBER));
		context.assertTrue(amberAspects.amount(terra) == 0 && amberAspects.amount(vinculum) == 10
						&& amberAspects.amount(vitreus) == 10,
				"amber mapping matches the TC6 runtime dump");
		AspectList crystalAspects = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(ThaumcraftContent.CRYSTAL_AER));
		context.assertTrue(crystalAspects.amount(aer) == 15 && crystalAspects.amount(vitreus) == 10,
				"crystal mapping matches the TC6 runtime dump");
		AspectList thaumiumAspects = AspectLookup.get().getAspects(context.getLevel(), new ItemStack(ThaumcraftContent.INGOT_THAUMIUM));
		context.assertTrue(thaumiumAspects.amount(ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("metallum"))) == 15
						&& thaumiumAspects.amount(ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("praecantatio"))) == 2
						&& thaumiumAspects.amount(thaumiumAspects.aspects().stream()
								.filter(aspect -> aspect.tag().equals("terra")).findFirst().orElseThrow()) == 2,
				"thaumium ingot mapping matches the TC6 runtime dump");
		context.succeed();
	}

	@GameTest
	public void crystalEssenceComponentAndCrystalDropsRoundTrip(GameTestHelper context) {
		Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(ThaumcraftRebornApi.id("aer"));
		ItemStack essence = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
		essence.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
		var ops = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, context.getLevel().registryAccess());
		var json = ItemStack.CODEC.encodeStart(ops, essence).getOrThrow();
		ItemStack restored = ItemStack.CODEC.parse(ops, json).getOrThrow();
		context.assertTrue(restored.get(ModDataComponents.CRYSTAL_ASPECT).equals(aspect),
				"crystal essence aspect component round trips through ItemStack codec");

		BlockPos pos = BlockPos.ZERO;
		var state = ThaumcraftContent.CRYSTAL_AER.defaultBlockState().setValue(PrimalCrystalBlock.SIZE, 2);
		var drops = Block.getDrops(state, context.getLevel(), pos, null);
		context.assertTrue(drops.size() == 3 && drops.stream()
						.allMatch(stack -> stack.get(ModDataComponents.CRYSTAL_ASPECT).equals(aspect)),
				"crystal cluster drops size plus one aspect-tagged essences");

		BlockPos supportPos = new BlockPos(3, 3, 3);
		BlockPos crystalPos = supportPos.above();
		context.getLevel().setBlock(supportPos, Blocks.STONE.defaultBlockState(), 3);
		context.getLevel().setBlock(crystalPos, state, 3);
		var selectionShape = context.getLevel().getBlockState(crystalPos)
				.getShape(context.getLevel(), crystalPos, CollisionContext.empty());
		context.assertTrue(selectionShape.bounds().maxY == 0.5,
				"crystal selection shape is the half-block facing its sole support");
		context.getLevel().setBlock(supportPos, Blocks.AIR.defaultBlockState(), 3);
		context.assertTrue(context.getLevel().getBlockState(crystalPos).isAir(),
				"crystal breaks when its final support is removed");
		context.succeed();
	}

	@GameTest
	public void stageCRecipeCodecsAndResearchGatesLoad(GameTestHelper context) {
		var recipes = context.getLevel().getServer().getRecipeManager();
		List<String> fixtureIds = List.of(
				"fixture_arcane_shaped", "fixture_arcane_shapeless", "fixture_crucible", "fixture_infusion"
		);
		for (String path : fixtureIds) {
			RecipeHolder<?> holder = recipes.byKey(recipeId("thaumcraft_reborn_test", path)).orElseThrow();
			context.assertTrue(holder.value() instanceof ThaumcraftRecipe, path + " recipe codec loaded");
		}
		long customRecipeCount = recipes.getRecipes().stream()
				.filter(holder -> holder.value() instanceof ThaumcraftRecipe)
				.count();
		context.assertTrue(customRecipeCount == 11, "four recipe serializers plus seven shipped crucible recipes load");

		ThaumcraftRecipe gated = (ThaumcraftRecipe) recipes.byKey(
				recipeId("thaumcraft_reborn_test", "fixture_arcane_shaped")
		).orElseThrow().value();
		ResearchGate gate = gated.gate().orElseThrow();
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		context.assertTrue(!RecipeGateAccess.get().isUnlocked(player, gate), "research gate is locked before grant");
		KnowledgeAccess.get().addResearch(player, gate.key());
		KnowledgeAccess.get().setResearchStage(player, gate.key(), gate.stage().orElseThrow());
		context.assertTrue(RecipeGateAccess.get().isUnlocked(player, gate), "research gate unlocks at the requested stage");
		context.assertTrue(
				ResearchGate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"FIRSTSTEPS@2\""))
						.getOrThrow().key().equals(ThaumcraftRebornApi.id("firststeps")),
				"TC6 uppercase research keys convert to namespaced lowercase identifiers"
		);
		context.succeed();
	}

	@GameTest
	public void stageCWorldgenAndCinnabarSmeltingAreRegistered(GameTestHelper context) {
		var placedFeatures = context.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
		for (String path : List.of("ore_amber", "ore_cinnabar", "ore_quartz", "primal_crystals")) {
			context.assertTrue(placedFeatures.get(ThaumcraftRebornApi.id(path)).isPresent(),
					path + " placed feature is registered");
		}
		RecipeHolder<?> smeltingHolder = context.getLevel().getServer().getRecipeManager()
				.byKey(recipeId("thaumcraft_reborn", "smelt_cinnabar")).orElseThrow();
		context.assertTrue(smeltingHolder.value() instanceof SmeltingRecipe, "cinnabar smelting recipe loads");
		ItemStack result = ((SmeltingRecipe) smeltingHolder.value())
				.assemble(new SingleRecipeInput(new ItemStack(ThaumcraftContent.ORE_CINNABAR)));
		context.assertTrue(result.is(ThaumcraftContent.QUICKSILVER), "cinnabar smelts to quicksilver");
		context.succeed();
	}

	private static ResourceKey<Recipe<?>> recipeId(String namespace, String path) {
		return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(namespace, path));
	}
}
