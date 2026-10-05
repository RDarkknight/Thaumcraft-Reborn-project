package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.chunk.LevelChunk;

public final class MinimalAuraSimulation implements AuraSimulation {
	private static final float[] PHASE_VIS = {0.25F, 0.15F, 0.1F, 0.05F, 0.0F, 0.05F, 0.1F, 0.15F};
	private static final float[] PHASE_MAX = {0.15F, 0.05F, 0.0F, -0.05F, -0.15F, -0.05F, 0.0F, 0.05F};

	@Override
	public void tick(ServerLevel level, LevelChunk chunk) {
		AuraChunkData data = chunk.getAttached(ModAttachments.AURA);
		if (data == null) {
			return;
		}
		Holder<WorldClock> overworldClock = level.registryAccess()
				.lookupOrThrow(Registries.WORLD_CLOCK)
				.getOrThrow(WorldClocks.OVERWORLD);
		long clockTicks = level.clockManager().getInstance(overworldClock).totalTicks();
		int phase = (int) Math.floorMod(
				Math.floorDiv(clockTicks, MoonPhase.PHASE_LENGTH),
				MoonPhase.COUNT
		);
		float phaseMax = 1.0F + PHASE_MAX[phase];
		float target = data.base() * phaseMax;
		float missing = target - (data.vis() + data.flux());
		if (missing > 0.0F) {
			chunk.setAttached(ModAttachments.AURA, data.withVis(data.vis() + Math.min(missing, PHASE_VIS[phase])));
		}
	}
}
