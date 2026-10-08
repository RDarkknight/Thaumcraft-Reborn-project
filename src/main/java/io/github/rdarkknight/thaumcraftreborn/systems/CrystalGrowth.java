package io.github.rdarkknight.thaumcraftreborn.systems;

import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.crystal.CrystalGrowthAccess;
import io.github.rdarkknight.thaumcraftreborn.content.PrimalCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalGrowth {
	private static final int AURA_THRESHOLD = 10;

	private CrystalGrowth() {
	}

	public static void init() {
		CrystalGrowthAccess.install(CrystalGrowth::tick);
	}

	public static void tick(Block block, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!(block instanceof PrimalCrystalBlock crystal)) {
			return;
		}
		int size = state.getValue(PrimalCrystalBlock.SIZE);
		int generation = state.getValue(PrimalCrystalBlock.GENERATION);
		if (random.nextInt(3 + generation) != 0) {
			return;
		}

		AuraAccess aura = AuraAccess.get();
		float vis = aura.getVis(level, pos);
		if (vis <= AURA_THRESHOLD) {
			if (size > 0) {
				level.setBlock(pos, state.setValue(PrimalCrystalBlock.SIZE, size - 1), Block.UPDATE_ALL);
				aura.addVis(level, pos, AURA_THRESHOLD);
			} else if (touchesCrystal(level, pos, block)) {
				level.destroyBlock(pos, false);
				aura.addVis(level, pos, AURA_THRESHOLD);
			}
			return;
		}

		if (vis <= aura.getBase(level, pos) + AURA_THRESHOLD) {
			return;
		}

		if (size < 3 && size < 5 - generation + Math.floorMod(pos.getY(), 3)) {
			if (aura.drainVis(level, pos, AURA_THRESHOLD, false) > 0.0F) {
				level.setBlock(pos, state.setValue(PrimalCrystalBlock.SIZE, size + 1), Block.UPDATE_ALL);
			}
			return;
		}

		if (generation >= 4) {
			return;
		}
		BlockPos target = spreadTarget(level, pos, random);
		if (target == null || aura.drainVis(level, pos, AURA_THRESHOLD, false) <= 0.0F) {
			return;
		}
		int nextGeneration = random.nextInt(6) == 0 ? Math.max(0, generation - 1) : generation;
		level.setBlock(target, crystal.defaultBlockState().setValue(PrimalCrystalBlock.GENERATION, nextGeneration + 1),
				Block.UPDATE_ALL);
	}

	private static BlockPos spreadTarget(ServerLevel level, BlockPos pos, RandomSource random) {
		BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
		if (target.equals(pos) || !level.getFluidState(target).isEmpty() || random.nextInt(16) != 0) {
			return null;
		}
		BlockState targetState = level.getBlockState(target);
		if (!targetState.isAir() && !targetState.getCollisionShape(level, target).isEmpty()) {
			return null;
		}
		for (Direction direction : Direction.values()) {
			BlockPos support = target.relative(direction);
			if (level.getBlockState(support).is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
					&& Block.isFaceFull(level.getBlockState(support).getCollisionShape(level, support), direction.getOpposite())) {
				return target;
			}
		}
		return null;
	}

	private static boolean touchesCrystal(ServerLevel level, BlockPos pos, Block block) {
		for (Direction direction : Direction.values()) {
			if (level.getBlockState(pos.relative(direction)).is(block)) {
				return true;
			}
		}
		return false;
	}
}
