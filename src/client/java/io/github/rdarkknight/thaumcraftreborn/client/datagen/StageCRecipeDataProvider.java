package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

final class StageCRecipeDataProvider implements DataProvider {
	private final PackOutput.PathProvider recipes;
	private CachedOutput cachedOutput;

	StageCRecipeDataProvider(FabricPackOutput output) {
		this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		this.cachedOutput = cachedOutput;
		List<CompletableFuture<?>> writes = new ArrayList<>();
		addShaped(writes, "thaumium_ingots_to_block", "thaumcraft_reborn:metal_thaumium", 1,
				new String[]{"III", "III", "III"}, Map.of('I', "thaumcraft_reborn:ingot_thaumium"));
		addShaped(writes, "thaumium_block_to_ingots", "thaumcraft_reborn:ingot_thaumium", 9,
				new String[]{"B"}, Map.of('B', "thaumcraft_reborn:metal_thaumium"));
		addShaped(writes, "thaumium_ingot_to_nuggets", "thaumcraft_reborn:nugget_thaumium", 9,
				new String[]{"I"}, Map.of('I', "thaumcraft_reborn:ingot_thaumium"));
		addShaped(writes, "thaumium_nuggets_to_ingot", "thaumcraft_reborn:ingot_thaumium", 1,
				new String[]{"NNN", "NNN", "NNN"}, Map.of('N', "thaumcraft_reborn:nugget_thaumium"));
		addShaped(writes, "amber_to_block", "thaumcraft_reborn:amber_block", 1,
				new String[]{"AA", "AA"}, Map.of('A', "thaumcraft_reborn:amber"));
		addShaped(writes, "amber_block_to_amber", "thaumcraft_reborn:amber", 4,
				new String[]{"B"}, Map.of('B', "thaumcraft_reborn:amber_block"));
		addShaped(writes, "amber_block_to_brick", "thaumcraft_reborn:amber_brick", 4,
				new String[]{"BB", "BB"}, Map.of('B', "thaumcraft_reborn:amber_block"));
		addShaped(writes, "amber_brick_to_blocks", "thaumcraft_reborn:amber_block", 4,
				new String[]{"BB", "BB"}, Map.of('B', "thaumcraft_reborn:amber_brick"));
		addShaped(writes, "arcane_stone", "thaumcraft_reborn:stone_arcane", 9,
				new String[]{"SSS", "SCS", "SSS"},
				Map.of('S', "minecraft:stone", 'C', "thaumcraft_reborn:crystal_essence"));
		addShaped(writes, "arcane_bricks", "thaumcraft_reborn:stone_arcane_brick", 4,
				new String[]{"SS", "SS"}, Map.of('S', "thaumcraft_reborn:stone_arcane"));
		addShaped(writes, "greatwood_planks", "thaumcraft_reborn:plank_greatwood", 4,
				new String[]{"L"}, Map.of('L', "thaumcraft_reborn:log_greatwood"));
		addShaped(writes, "silverwood_planks", "thaumcraft_reborn:plank_silverwood", 4,
				new String[]{"L"}, Map.of('L', "thaumcraft_reborn:log_silverwood"));
		addSmelting(writes, "smelt_amber", "thaumcraft_reborn:ore_amber", "thaumcraft_reborn:amber", 1.0F);
		addSmelting(writes, "smelt_cinnabar", "thaumcraft_reborn:ore_cinnabar", "thaumcraft_reborn:quicksilver", 1.0F);
		addSmelting(writes, "smelt_quartz", "thaumcraft_reborn:ore_quartz", "minecraft:quartz", 1.0F);
		addSmelting(writes, "smelt_log_greatwood", "thaumcraft_reborn:log_greatwood", "minecraft:charcoal", 0.5F);
		addSmelting(writes, "smelt_log_silverwood", "thaumcraft_reborn:log_silverwood", "minecraft:charcoal", 0.5F);
		addShaped(writes, "thaumium_sword", "thaumcraft_reborn:thaumium_sword", 1,
				new String[]{"I", "I", "S"},
				Map.of('I', "thaumcraft_reborn:ingot_thaumium", 'S', "#c:rods/wooden"));
		addShaped(writes, "thaumium_shovel", "thaumcraft_reborn:thaumium_shovel", 1,
				new String[]{"I", "S", "S"},
				Map.of('I', "thaumcraft_reborn:ingot_thaumium", 'S', "#c:rods/wooden"));
		addShaped(writes, "thaumium_pick", "thaumcraft_reborn:thaumium_pick", 1,
				new String[]{"III", " S ", " S "},
				Map.of('I', "thaumcraft_reborn:ingot_thaumium", 'S', "#c:rods/wooden"));
		addShaped(writes, "thaumium_axe", "thaumcraft_reborn:thaumium_axe", 1,
				new String[]{"II", "SI", "S "},
				Map.of('I', "thaumcraft_reborn:ingot_thaumium", 'S', "#c:rods/wooden"));
		addShaped(writes, "thaumium_hoe", "thaumcraft_reborn:thaumium_hoe", 1,
				new String[]{"II", "S ", "S "},
				Map.of('I', "thaumcraft_reborn:ingot_thaumium", 'S', "#c:rods/wooden"));
		return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
	}

	private void addShaped(List<CompletableFuture<?>> writes, String id, String result, int count, String[] pattern,
			Map<Character, String> key) {
		JsonObject recipe = new JsonObject();
		recipe.addProperty("type", "minecraft:crafting_shaped");
		JsonArray rows = new JsonArray();
		for (String row : pattern) {
			rows.add(row);
		}
		recipe.add("pattern", rows);
		JsonObject keyJson = new JsonObject();
		key.forEach((symbol, item) -> keyJson.addProperty(symbol.toString(), item));
		recipe.add("key", keyJson);
		recipe.add("result", result(result, count));
		writes.add(DataProvider.saveStable(cachedOutput, recipe, recipes.json(ThaumcraftReborn.id(id))));
	}

	private void addSmelting(
			List<CompletableFuture<?>> writes,
			String id,
			String ingredientId,
			String resultId,
			float experience
	) {
		JsonObject recipe = new JsonObject();
		recipe.addProperty("type", "minecraft:smelting");
		recipe.addProperty("category", "misc");
		recipe.addProperty("ingredient", ingredientId);
		recipe.add("result", result(resultId, 1));
		recipe.addProperty("experience", experience);
		recipe.addProperty("cookingtime", 200);
		writes.add(DataProvider.saveStable(cachedOutput, recipe, recipes.json(ThaumcraftReborn.id(id))));
	}

	private static JsonObject result(String item, int count) {
		JsonObject result = new JsonObject();
		result.addProperty("id", item);
		if (count != 1) {
			result.addProperty("count", count);
		}
		return result;
	}

	@Override
	public String getName() {
		return "Thaumcraft Reborn Stage C Recipes";
	}
}
