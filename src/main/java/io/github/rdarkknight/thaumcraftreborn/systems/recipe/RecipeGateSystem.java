package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import io.github.rdarkknight.thaumcraftreborn.api.recipe.RecipeGateAccess;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import net.minecraft.world.entity.player.Player;

public final class RecipeGateSystem {
	private RecipeGateSystem() {
	}

	public static void init() {
		RecipeGateAccess.install(RecipeGateSystem::isUnlocked);
	}

	private static boolean isUnlocked(Player player, ResearchGate gate) {
		return ResearchProgression.isUnlocked(player, gate.key(), gate.stage());
	}
}
