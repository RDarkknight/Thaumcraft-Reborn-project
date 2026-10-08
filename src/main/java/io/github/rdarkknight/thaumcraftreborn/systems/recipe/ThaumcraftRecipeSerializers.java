package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

final class ThaumcraftRecipeSerializers {
	private static final RecipeType<ThaumcraftRecipe> ARCANE_SHAPED_TYPE = type("arcane_shaped");
	private static final RecipeType<ThaumcraftRecipe> ARCANE_SHAPELESS_TYPE = type("arcane_shapeless");
	private static final RecipeType<ThaumcraftRecipe> CRUCIBLE_TYPE = type("crucible");
	private static final RecipeType<ThaumcraftRecipe> INFUSION_TYPE = type("infusion");

	private static RecipeSerializer<ThaumcraftRecipe> arcaneShaped;
	private static RecipeSerializer<ThaumcraftRecipe> arcaneShapeless;
	private static RecipeSerializer<ThaumcraftRecipe> crucible;
	private static RecipeSerializer<ThaumcraftRecipe> infusion;

	private ThaumcraftRecipeSerializers() {
	}

	static void init() {
		arcaneShaped = register("arcane_shaped", ThaumcraftRecipe.Kind.ARCANE_SHAPED);
		arcaneShapeless = register("arcane_shapeless", ThaumcraftRecipe.Kind.ARCANE_SHAPELESS);
		crucible = register("crucible", ThaumcraftRecipe.Kind.CRUCIBLE);
		infusion = register("infusion", ThaumcraftRecipe.Kind.INFUSION);
	}

	static RecipeSerializer<ThaumcraftRecipe> serializer(ThaumcraftRecipe.Kind kind) {
		return switch (kind) {
			case ARCANE_SHAPED -> arcaneShaped;
			case ARCANE_SHAPELESS -> arcaneShapeless;
			case CRUCIBLE -> crucible;
			case INFUSION -> infusion;
		};
	}

	static RecipeType<ThaumcraftRecipe> type(ThaumcraftRecipe.Kind kind) {
		return switch (kind) {
			case ARCANE_SHAPED -> ARCANE_SHAPED_TYPE;
			case ARCANE_SHAPELESS -> ARCANE_SHAPELESS_TYPE;
			case CRUCIBLE -> CRUCIBLE_TYPE;
			case INFUSION -> INFUSION_TYPE;
		};
	}

	private static RecipeSerializer<ThaumcraftRecipe> register(String path, ThaumcraftRecipe.Kind kind) {
		var codec = ThaumcraftRecipe.codec(kind);
		return Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				ThaumcraftReborn.id(path),
				new RecipeSerializer<>(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()))
		);
	}

	private static RecipeType<ThaumcraftRecipe> type(String path) {
		return new RecipeType<>() {
			@Override
			public String toString() {
				return ThaumcraftReborn.id(path).toString();
			}
		};
	}
}
