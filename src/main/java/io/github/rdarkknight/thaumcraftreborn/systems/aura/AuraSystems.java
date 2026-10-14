package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

public final class AuraSystems {
	private static final Map<ServerLevel, Map<Integer, Set<LevelChunk>>> LOADED_CHUNKS = new ConcurrentHashMap<>();
	private static final Map<ServerLevel, Long> LAST_SIMULATION_TIME = new ConcurrentHashMap<>();
	private static final AuraSimulation SIMULATION = new Tc6AuraSimulation();
	private static final Map<ServerLevel, TickMetrics> LAST_TICK_METRICS = new ConcurrentHashMap<>();

	private AuraSystems() {
	}

	public static void init() {
		BiomeAuraTypes.init();
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyLoaded) -> {
			loadedChunks(level, bucket(chunk.getPos())).add(chunk);
			if (!chunk.hasAttached(ModAttachments.AURA)) {
				chunk.setAttached(ModAttachments.AURA, generateData(level, chunk));
			}
		});
		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			Map<Integer, Set<LevelChunk>> chunksByBucket = LOADED_CHUNKS.get(level);
			Set<LevelChunk> chunks = chunksByBucket == null ? null : chunksByBucket.get(bucket(chunk.getPos()));
			if (chunks != null) {
				chunks.remove(chunk);
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(AuraSystems::tickLevel);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LOADED_CHUNKS.clear();
			LAST_SIMULATION_TIME.clear();
			LAST_TICK_METRICS.clear();
		});
	}

	public static void tick(ServerLevel level, LevelChunk chunk) {
		SIMULATION.tick(level, chunk);
	}

	public static TickMetrics lastTickMetrics(ServerLevel level) {
		return LAST_TICK_METRICS.getOrDefault(level, new TickMetrics(-1L, Tc6AuraSimulation.phaseIndex(level), 0L, 0));
	}

	public static AuraChunkData generateData(ServerLevel level, LevelChunk chunk) {
		ChunkPos pos = chunk.getPos();
		int centerX = pos.getMiddleBlockX();
		int centerZ = pos.getMiddleBlockZ();
		float modifier = 0.0F;
		modifier += modifierAt(level, centerX, centerZ);
		modifier += modifierAt(level, centerX - 16, centerZ);
		modifier += modifierAt(level, centerX + 16, centerZ);
		modifier += modifierAt(level, centerX, centerZ - 16);
		modifier += modifierAt(level, centerX, centerZ + 16);
		modifier /= 5.0F;
		RandomSource random = level.getRandom();
		float noise = (float) (1.0D + random.nextGaussian() * 0.1D);
		short base = (short) Math.clamp((int) (modifier * 500.0F * noise), 0, 500);
		return new AuraChunkData(base, base, 0.0F);
	}

	private static float modifierAt(ServerLevel level, int blockX, int blockZ) {
		Holder<Biome> biome = level.getUncachedNoiseBiome(
				QuartPos.fromBlock(blockX),
				QuartPos.fromBlock(50),
				QuartPos.fromBlock(blockZ)
		);
		return BiomeAuraTypes.modifier(biome, false);
	}

	public static void tickLevel(ServerLevel level) {
		long gameTime = level.getLevelData().getGameTime();
		Long previous = LAST_SIMULATION_TIME.put(level, gameTime);
		if (previous != null && previous == gameTime) {
			return;
		}
		long startedAt = System.nanoTime();
		int phase = Tc6AuraSimulation.phaseIndex(level);
		int bucket = (int) Math.floorMod(gameTime, 20L);
		Set<LevelChunk> chunks = LOADED_CHUNKS.getOrDefault(level, Map.of()).get(bucket);
		int processed = 0;
		if (chunks != null) {
			for (LevelChunk chunk : chunks) {
				if (chunk.getAttached(ModAttachments.AURA) == null) {
					continue;
				}
				SIMULATION.tick(level, chunk);
				processed++;
			}
		}
		LAST_TICK_METRICS.put(level, new TickMetrics(
				gameTime,
				phase,
				System.nanoTime() - startedAt,
				processed
		));
	}

	private static Set<LevelChunk> loadedChunks(ServerLevel level, int bucket) {
		return LOADED_CHUNKS.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>())
				.computeIfAbsent(bucket, ignored -> ConcurrentHashMap.newKeySet());
	}

	private static int bucket(ChunkPos pos) {
		return Math.floorMod(pos.x() * 31 + pos.z(), 20);
	}

	public record TickMetrics(long gameTime, int phase, long nanoseconds, int chunksProcessed) {
	}
}
