package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

public final class RecipeSystems {
	private RecipeSystems() {
	}

	public static void init() {
		RecipeGateSystem.init();
		ThaumcraftRecipeSerializers.init();
	}
}
