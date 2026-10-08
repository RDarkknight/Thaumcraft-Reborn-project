package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

public final class AuraSystems {
	private static final Map<ServerLevel, Set<LevelChunk>> LOADED_CHUNKS = new ConcurrentHashMap<>();
	private static final Map<ServerLevel, Long> LAST_SIMULATION_TIME = new ConcurrentHashMap<>();
	private static final AuraSimulation SIMULATION = new MinimalAuraSimulation();

	private AuraSystems() {
	}

	public static void init() {
		BiomeAuraTypes.init();
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyLoaded) -> {
			LOADED_CHUNKS.computeIfAbsent(level, ignored -> ConcurrentHashMap.newKeySet()).add(chunk);
			if (!chunk.hasAttached(ModAttachments.AURA)) {
				chunk.setAttached(ModAttachments.AURA, generateData(level, chunk));
			}
		});
		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			Set<LevelChunk> chunks = LOADED_CHUNKS.get(level);
			if (chunks != null) {
				chunks.remove(chunk);
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(AuraSystems::tickLevel);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LOADED_CHUNKS.clear();
			LAST_SIMULATION_TIME.clear();
		});
	}

	public static void tick(ServerLevel level, LevelChunk chunk) {
		SIMULATION.tick(level, chunk);
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

	private static void tickLevel(ServerLevel level) {
		long gameTime = level.getLevelData().getGameTime();
		Long previous = LAST_SIMULATION_TIME.put(level, gameTime);
		if (previous == null || gameTime <= previous || gameTime % 20L != 0L) {
			return;
		}
		Set<LevelChunk> chunks = LOADED_CHUNKS.get(level);
		if (chunks != null) {
			for (LevelChunk chunk : chunks) {
				SIMULATION.tick(level, chunk);
			}
		}
	}
}
