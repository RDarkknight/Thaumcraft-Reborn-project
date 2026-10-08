package io.github.rdarkknight.thaumcraftreborn.api.crystal;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface CrystalGrowthAccess {
	CrystalGrowthAccess NONE = (block, state, level, pos, random) -> {
	};

	static CrystalGrowthAccess get() {
		return Holder.INSTANCE;
	}

	static void install(CrystalGrowthAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	void tick(Block block, BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

	final class Holder {
		private static volatile CrystalGrowthAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
