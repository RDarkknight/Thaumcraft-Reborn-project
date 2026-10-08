package io.github.rdarkknight.thaumcraftreborn.api.aura;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface AuraAccess {
	AuraAccess NONE = new AuraAccess() {
		@Override
		public float getVis(Level level, BlockPos pos) {
			return 0.0F;
		}

		@Override
		public float getFlux(Level level, BlockPos pos) {
			return 0.0F;
		}

		@Override
		public int getBase(Level level, BlockPos pos) {
			return 0;
		}

		@Override
		public float getTotalAura(Level level, BlockPos pos) {
			return 0.0F;
		}

		@Override
		public float getFluxSaturation(Level level, BlockPos pos) {
			return 0.0F;
		}

		@Override
		public float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
			return 0.0F;
		}

		@Override
		public float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
			return 0.0F;
		}

		@Override
		public void addVis(Level level, BlockPos pos, float amount) {
		}

		@Override
		public void addFlux(Level level, BlockPos pos, float amount, boolean showEffect) {
		}

		@Override
		public boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
			return false;
		}
	};

	static AuraAccess get() {
		return Holder.INSTANCE;
	}

	static void install(AuraAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	float getVis(Level level, BlockPos pos);

	float getFlux(Level level, BlockPos pos);

	int getBase(Level level, BlockPos pos);

	float getTotalAura(Level level, BlockPos pos);

	float getFluxSaturation(Level level, BlockPos pos);

	float drainVis(Level level, BlockPos pos, float amount, boolean simulate);

	float drainFlux(Level level, BlockPos pos, float amount, boolean simulate);

	void addVis(Level level, BlockPos pos, float amount);

	void addFlux(Level level, BlockPos pos, float amount, boolean showEffect);

	boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos);

	final class Holder {
		private static volatile AuraAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
