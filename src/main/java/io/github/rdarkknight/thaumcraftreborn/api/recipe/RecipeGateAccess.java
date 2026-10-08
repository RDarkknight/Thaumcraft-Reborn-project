package io.github.rdarkknight.thaumcraftreborn.api.recipe;

import java.util.Objects;
import net.minecraft.world.entity.player.Player;

public interface RecipeGateAccess {
	RecipeGateAccess NONE = (player, gate) -> false;

	boolean isUnlocked(Player player, ResearchGate gate);

	static RecipeGateAccess get() {
		return Holder.INSTANCE;
	}

	static void install(RecipeGateAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	final class Holder {
		private static volatile RecipeGateAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
