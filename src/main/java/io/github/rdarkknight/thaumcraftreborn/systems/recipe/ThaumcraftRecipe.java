package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record ThaumcraftRecipe(
		Kind kind,
		ItemStackTemplate result,
		List<Ingredient> ingredients,
		int width,
		int height,
		List<Ingredient> crystalComponents,
		Optional<Ingredient> catalyst,
		Optional<Ingredient> centralIngredient,
		AspectList aspects,
		int instability,
		Optional<ResearchGate> gate,
		int visCost,
		String group,
		boolean showNotification
) implements Recipe<RecipeInput> {
	public ThaumcraftRecipe {
		ingredients = List.copyOf(ingredients);
		crystalComponents = List.copyOf(crystalComponents);
		catalyst = catalyst == null ? Optional.empty() : catalyst;
		centralIngredient = centralIngredient == null ? Optional.empty() : centralIngredient;
		aspects = aspects == null ? AspectList.EMPTY : aspects;
		gate = gate == null ? Optional.empty() : gate;
		group = group == null ? "" : group;
	}

	static MapCodec<ThaumcraftRecipe> codec(Kind kind) {
		var componentsField = Ingredient.CODEC.listOf().optionalFieldOf(
				kind == Kind.INFUSION ? "components" : "crystal_components",
				List.of()
		);
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				ItemStackTemplate.CODEC.fieldOf("result").forGetter(ThaumcraftRecipe::result),
				Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(ThaumcraftRecipe::ingredients),
				Codec.INT.optionalFieldOf("width", 0).forGetter(ThaumcraftRecipe::width),
				Codec.INT.optionalFieldOf("height", 0).forGetter(ThaumcraftRecipe::height),
				componentsField.forGetter(ThaumcraftRecipe::crystalComponents),
				Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(ThaumcraftRecipe::catalyst),
				Ingredient.CODEC.optionalFieldOf("central_ingredient").forGetter(ThaumcraftRecipe::centralIngredient),
				AspectList.CODEC.optionalFieldOf("aspects", AspectList.EMPTY).forGetter(ThaumcraftRecipe::aspects),
				Codec.INT.optionalFieldOf("instability", 0).forGetter(ThaumcraftRecipe::instability),
				ResearchGate.CODEC.optionalFieldOf("research").forGetter(ThaumcraftRecipe::gate),
				Codec.INT.optionalFieldOf("vis_cost", 0).forGetter(ThaumcraftRecipe::visCost),
				Codec.STRING.optionalFieldOf("group", "").forGetter(ThaumcraftRecipe::group),
				Codec.BOOL.optionalFieldOf("show_notification", false).forGetter(ThaumcraftRecipe::showNotification)
		).apply(instance, (result, ingredients, width, height, crystalComponents, catalyst, centralIngredient,
				aspects, instability, gate, visCost, group, showNotification) -> new ThaumcraftRecipe(
						kind, result, ingredients, width, height, crystalComponents, catalyst, centralIngredient,
						aspects, instability, gate, visCost, group, showNotification
				)));
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return switch (kind) {
			case ARCANE_SHAPED -> matchesShaped(input);
			case ARCANE_SHAPELESS -> matchesShapeless(input);
			case CRUCIBLE, INFUSION -> false;
		};
	}

	private boolean matchesShapeless(RecipeInput input) {
		if (input.size() != ingredients.size()) {
			return false;
		}
		return matchIngredient(0, input, new boolean[input.size()]);
	}

	private boolean matchIngredient(int index, RecipeInput input, boolean[] used) {
		if (index == ingredients.size()) {
			return true;
		}
		Ingredient ingredient = ingredients.get(index);
		for (int slot = 0; slot < input.size(); slot++) {
			if (!used[slot] && ingredient.test(input.getItem(slot))) {
				used[slot] = true;
				if (matchIngredient(index + 1, input, used)) {
					return true;
				}
				used[slot] = false;
			}
		}
		return false;
	}

	private boolean matchesShaped(RecipeInput input) {
		if (!(input instanceof CraftingInput crafting) || width <= 0 || height <= 0
				|| ingredients.size() != width * height || width > crafting.width() || height > crafting.height()) {
			return false;
		}
		for (int offsetX = 0; offsetX <= crafting.width() - width; offsetX++) {
			for (int offsetY = 0; offsetY <= crafting.height() - height; offsetY++) {
				if (matchesPattern(crafting, offsetX, offsetY, false) || matchesPattern(crafting, offsetX, offsetY, true)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean matchesPattern(CraftingInput input, int offsetX, int offsetY, boolean mirrored) {
		for (int y = 0; y < input.height(); y++) {
			for (int x = 0; x < input.width(); x++) {
				int recipeX = x - offsetX;
				int recipeY = y - offsetY;
				if (recipeX >= 0 && recipeY >= 0 && recipeX < width && recipeY < height) {
					if (mirrored) {
						recipeX = width - recipeX - 1;
					}
					if (!ingredients.get(recipeX + recipeY * width).test(input.getItem(x + y * input.width()))) {
						return false;
					}
				} else if (!input.getItem(x + y * input.width()).isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return result.create();
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return ThaumcraftRecipeSerializers.serializer(kind);
	}

	@Override
	public RecipeType<? extends Recipe<RecipeInput>> getType() {
		return ThaumcraftRecipeSerializers.type(kind);
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return new RecipeBookCategory();
	}

	enum Kind {
		ARCANE_SHAPED,
		ARCANE_SHAPELESS,
		CRUCIBLE,
		INFUSION
	}
}
