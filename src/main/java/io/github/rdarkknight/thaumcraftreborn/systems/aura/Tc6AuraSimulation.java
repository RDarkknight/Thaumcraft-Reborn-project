package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.chunk.LevelChunk;

public final class Tc6AuraSimulation implements AuraSimulation {
	private static final float[] PHASE_VIS = {0.25F, 0.15F, 0.1F, 0.05F, 0.0F, 0.05F, 0.1F, 0.15F};
	private static final float[] PHASE_MAX = {0.15F, 0.05F, 0.0F, -0.05F, -0.15F, -0.05F, 0.0F, 0.05F};

	@Override
	public void tick(ServerLevel level, LevelChunk chunk) {
		tick(level, chunk, phaseIndex(level), level.getRandom());
	}

	public void tick(ServerLevel level, LevelChunk chunk, int phase, RandomSource random) {
		AuraChunkData data = chunk.getAttached(ModAttachments.AURA);
		if (data == null) {
			return;
		}
		float phaseMax = 1.0F + PHASE_MAX[phase];
		float phaseVis = PHASE_VIS[phase];
		float phaseFlux = 0.25F - phaseVis;
		int chunkX = chunk.getPos().x();
		int chunkZ = chunk.getPos().z();
		int[] directions = shuffledDirections(random);
		LevelChunk lowestVisChunk = null;
		LevelChunk lowestFluxChunk = null;
		float lowestVis = Float.MAX_VALUE;
		float lowestFlux = Float.MAX_VALUE;
		ServerChunkCache chunkSource = level.getChunkSource();

		for (int direction : directions) {
			int offsetX = switch (direction) {
				case 0 -> 1;
				case 1 -> -1;
				default -> 0;
			};
			int offsetZ = switch (direction) {
				case 2 -> 1;
				case 3 -> -1;
				default -> 0;
			};
			LevelChunk neighbor = chunkSource.getChunkNow(chunkX + offsetX, chunkZ + offsetZ);
			if (neighbor == null) {
				continue;
			}
			AuraChunkData neighborData = neighbor.getAttached(ModAttachments.AURA);
			if (neighborData == null) {
				continue;
			}
			if ((lowestVisChunk == null || lowestVis > neighborData.vis())
					&& neighborData.vis() + neighborData.flux() < neighborData.base() * phaseMax) {
				lowestVisChunk = neighbor;
				lowestVis = neighborData.vis();
			}
			if (lowestFluxChunk == null || lowestFlux > neighborData.flux()) {
				lowestFluxChunk = neighbor;
				lowestFlux = neighborData.flux();
			}
		}

		float currentVis = data.vis();
		float currentFlux = data.flux();
		if (lowestVisChunk != null && lowestVis < currentVis && (double) (lowestVis / currentVis) < 0.75D) {
			float moved = Math.min(currentVis - lowestVis, 1.0F);
			currentVis -= moved;
			AuraChunkData neighborData = lowestVisChunk.getAttached(ModAttachments.AURA);
			lowestVisChunk.setAttached(ModAttachments.AURA, neighborData.withVis(lowestVis + moved));
		}
		if (lowestFluxChunk != null
				&& currentFlux > Math.max(5.0F, data.base() / 10.0F)
				&& (double) lowestFlux < (double) currentFlux / 1.75D) {
			float moved = Math.min(currentFlux - lowestFlux, 1.0F);
			currentFlux -= moved;
			AuraChunkData neighborData = lowestFluxChunk.getAttached(ModAttachments.AURA);
			lowestFluxChunk.setAttached(ModAttachments.AURA, neighborData.withFlux(lowestFlux + moved));
		}

		float base = data.base() * phaseMax;
		if (currentVis + currentFlux < base) {
			currentVis += Math.min(base - (currentVis + currentFlux), phaseVis);
		} else if ((double) currentVis > (double) base * 1.25D && random.nextFloat() < 0.1F) {
			currentFlux += phaseFlux;
			currentVis -= phaseFlux;
		} else if ((double) currentVis <= (double) base * 0.1D
				&& currentVis >= currentFlux && random.nextFloat() < 0.1F) {
			currentFlux += phaseFlux;
		}

		if (currentVis != data.vis() || currentFlux != data.flux()) {
			chunk.setAttached(ModAttachments.AURA, new AuraChunkData(data.base(), currentVis, currentFlux));
		}
	}

	public static int phaseIndex(ServerLevel level) {
		Holder<WorldClock> overworldClock = level.registryAccess()
				.lookupOrThrow(Registries.WORLD_CLOCK)
				.getOrThrow(WorldClocks.OVERWORLD);
		long clockTicks = level.clockManager().getInstance(overworldClock).totalTicks();
		return (int) Math.floorMod(
				Math.floorDiv(clockTicks, MoonPhase.PHASE_LENGTH),
				MoonPhase.COUNT
		);
	}

	public static float phaseVis(int phase) {
		return PHASE_VIS[phase];
	}

	public static float phaseMax(int phase) {
		return 1.0F + PHASE_MAX[phase];
	}

	public static float phaseFlux(int phase) {
		return 0.25F - PHASE_VIS[phase];
	}

	private static int[] shuffledDirections(RandomSource random) {
		int[] directions = {0, 1, 2, 3};
		for (int index = directions.length - 1; index > 0; index--) {
			int other = random.nextInt(index + 1);
			int direction = directions[index];
			directions[index] = directions[other];
			directions[other] = direction;
		}
		return directions;
	}
}
