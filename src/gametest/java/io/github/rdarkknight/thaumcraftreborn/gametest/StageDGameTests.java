package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.AuraSystems;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.Tc6AuraSimulation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class StageDGameTests implements CustomTestMethodInvoker {
	@GameTest
	public void auraRegenerationUsesEveryMoonPhase(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		setCardinalAura(level, chunk, new AuraChunkData((short) 100, 300.0F, 300.0F));
		Tc6AuraSimulation simulation = new Tc6AuraSimulation();

		for (int phase = 0; phase < 8; phase++) {
			float target = 100.0F * Tc6AuraSimulation.phaseMax(phase);
			float initialVis = target - 0.05F;
			float expected = initialVis + Math.min(0.05F, Tc6AuraSimulation.phaseVis(phase));
			chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, initialVis, 0.0F));
			simulation.tick(level, chunk, phase, RandomSource.create(phase));
			context.assertTrue(
					Math.abs(chunk.getAttached(ModAttachments.AURA).vis() - expected) < 0.0001F,
					"vis regeneration follows moon phase " + phase
			);
		}
		context.succeed();
	}

	@GameTest
	public void auraFluxConversionsUseThePhaseFluxAmount(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		setCardinalAura(level, chunk, new AuraChunkData((short) 0, 300.0F, 300.0F));
		Tc6AuraSimulation simulation = new Tc6AuraSimulation();
		AuraChunkData start = new AuraChunkData((short) 0, 2.0F, 0.0F);
		chunk.setAttached(ModAttachments.AURA, start);

		for (int seed = 0; seed < 1_000 && chunk.getAttached(ModAttachments.AURA).flux() == 0.0F; seed++) {
			chunk.setAttached(ModAttachments.AURA, start);
			simulation.tick(level, chunk, 1, RandomSource.create(seed));
		}
		AuraChunkData converted = chunk.getAttached(ModAttachments.AURA);
		context.assertTrue(converted.flux() == Tc6AuraSimulation.phaseFlux(1)
						&& converted.vis() == 2.0F - Tc6AuraSimulation.phaseFlux(1),
				"over-base vis converts to flux with the phase amount");

		AuraChunkData lowVis = new AuraChunkData((short) 0, 0.0F, 0.0F);
		boolean lowVisConverted = false;
		for (int seed = 0; seed < 1_000 && !lowVisConverted; seed++) {
			chunk.setAttached(ModAttachments.AURA, lowVis);
			simulation.tick(level, chunk, 1, RandomSource.create(seed));
			lowVisConverted = chunk.getAttached(ModAttachments.AURA).flux() == Tc6AuraSimulation.phaseFlux(1);
		}
		context.assertTrue(lowVisConverted, "the low-vis conversion adds phase flux without subtracting vis");
		context.succeed();
	}

	@GameTest
	public void auraVisDiffusionUsesTheStrictThreeQuarterThreshold(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		setCardinalAura(level, chunk, new AuraChunkData((short) 100, 75.0F, 0.0F));
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 100.0F, 0.0F));
		new Tc6AuraSimulation().tick(level, chunk, 1, RandomSource.create(1L));

		context.assertTrue(chunk.getAttached(ModAttachments.AURA).vis() > 100.0F,
				"a ratio of exactly 0.75 does not diffuse vis");
		for (LevelChunk neighbor : cardinalChunks(level, chunk)) {
			context.assertTrue(neighbor.getAttached(ModAttachments.AURA).vis() == 75.0F,
					"threshold neighbors keep their vis");
		}
		context.succeed();
	}

	@GameTest
	public void auraVisDiffusionTransfersBelowTheThreeQuarterThreshold(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		setCardinalAura(level, chunk, new AuraChunkData((short) 100, 74.0F, 0.0F));
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 100.0F, 0.0F));
		new Tc6AuraSimulation().tick(level, chunk, 1, RandomSource.create(1L));

		context.assertTrue(Math.abs(chunk.getAttached(ModAttachments.AURA).vis() - 99.15F) < 0.0001F,
				"a source transfers one vis before regenerating");
		context.assertTrue(cardinalChunks(level, chunk).stream()
						.mapToDouble(neighbor -> neighbor.getAttached(ModAttachments.AURA).vis())
						.sum() == 297.0D,
				"only one lowest-vis neighbor receives the transfer");
		context.succeed();
	}

	@GameTest
	public void auraFluxDiffusionUsesItsStrictThresholdAndClamps(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(BlockPos.ZERO);
		setCardinalAura(level, chunk, new AuraChunkData((short) 100, 100.0F, 0.0F));
		Tc6AuraSimulation simulation = new Tc6AuraSimulation();

		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 100.0F, 10.0F));
		simulation.tick(level, chunk, 1, RandomSource.create(2L));
		context.assertTrue(chunk.getAttached(ModAttachments.AURA).flux() == 10.0F,
				"flux at max(5, base / 10) does not diffuse");

		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 100.0F, 11.0F));
		simulation.tick(level, chunk, 1, RandomSource.create(3L));
		context.assertTrue(chunk.getAttached(ModAttachments.AURA).flux() == 10.0F,
				"flux above max(5, base / 10) transfers one unit");

		setCardinalAura(level, chunk, new AuraChunkData((short) 0, 300.0F, 32766.0F));
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 0, 2.0F, 32765.95F));
		for (int seed = 0; seed < 1_000 && chunk.getAttached(ModAttachments.AURA).flux() < 32766.0F; seed++) {
			chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 0, 2.0F, 32765.95F));
			simulation.tick(level, chunk, 1, RandomSource.create(seed));
		}
		context.assertTrue(chunk.getAttached(ModAttachments.AURA).flux() == 32766.0F,
				"simulation clamps flux at the aura limit");
		context.succeed();
	}

	@GameTest
	public void auraSimulationDoesNotLoadCardinalNeighbors(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		LevelChunk chunk = level.getChunkAt(new BlockPos(80_008, 64, 80_008));
		List<ChunkPos> unloaded = new ArrayList<>();
		int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (int[] offset : offsets) {
			int x = chunk.getPos().x() + offset[0];
			int z = chunk.getPos().z() + offset[1];
			LevelChunk neighbor = level.getChunkSource().getChunkNow(x, z);
			if (neighbor == null) {
				unloaded.add(new ChunkPos(x, z));
			} else {
				neighbor.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 300.0F, 300.0F));
			}
		}
		context.assertTrue(!unloaded.isEmpty(), "fixture has unloaded cardinal neighbors");
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 100.0F, 0.0F));
		new Tc6AuraSimulation().tick(level, chunk, 1, RandomSource.create(4L));
		context.assertTrue(unloaded.stream().allMatch(pos -> level.getChunkSource().getChunkNow(pos.x(), pos.z()) == null),
				"simulation leaves unloaded neighbors unloaded");
		context.succeed();
	}

	@GameTest
	public void auraSchedulerProcessesTheMatchingBucketOnlyOncePerGameTick(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		Holder<WorldClock> overworldClock = level.registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK)
				.getOrThrow(WorldClocks.OVERWORLD);
		level.clockManager().setTotalTicks(overworldClock, 0L);
		long nextGameTime = level.getLevelData().getGameTime() + 1L;
		int wantedBucket = (int) Math.floorMod(nextGameTime, 20L);
		int chunkX = 0;
		while (Math.floorMod(chunkX * 31, 20) != wantedBucket) {
			chunkX++;
		}
		LevelChunk chunk = level.getChunkAt(new BlockPos(chunkX * 16 + 8, 64, 8));
		setCardinalAura(level, chunk, new AuraChunkData((short) 100, 300.0F, 300.0F));
		chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 100, 99.0F, 0.0F));

		context.runAfterDelay(1, () -> {
			AuraSystems.tickLevel(level);
			float firstResult = chunk.getAttached(ModAttachments.AURA).vis();
			AuraSystems.tickLevel(level);
			AuraSystems.TickMetrics metrics = AuraSystems.lastTickMetrics(level);
			context.assertTrue(Math.abs(firstResult - 99.25F) < 0.0001F,
					"the current bucket receives one full-moon simulation step");
			context.assertTrue(chunk.getAttached(ModAttachments.AURA).vis() == firstResult,
					"the same level game tick is not simulated twice");
			context.assertTrue(metrics.gameTime() == nextGameTime && metrics.phase() == 0
							&& metrics.chunksProcessed() > 0,
					"tick diagnostics report phase and processed chunks");
			context.succeed();
		});
	}

	@GameTest
	public void auraAttachmentDataSurvivesCodecRoundTrip(GameTestHelper context) {
		AuraChunkData original = new AuraChunkData((short) 237, 123.5F, 17.25F);
		var encoded = AuraChunkData.CODEC.encodeStart(JsonOps.INSTANCE, original).result().orElseThrow();
		AuraChunkData decoded = AuraChunkData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
		context.assertTrue(decoded.equals(original), "aura base, vis, and flux persist through the data codec");
		context.assertTrue(ModAttachments.AURA.isPersistent(), "aura chunk attachment is persistent");
		context.succeed();
	}

	private static void setCardinalAura(ServerLevel level, LevelChunk chunk, AuraChunkData data) {
		for (LevelChunk neighbor : cardinalChunks(level, chunk)) {
			neighbor.setAttached(ModAttachments.AURA, data);
		}
	}

	private static List<LevelChunk> cardinalChunks(ServerLevel level, LevelChunk chunk) {
		int x = chunk.getPos().x();
		int z = chunk.getPos().z();
		return List.of(
				level.getChunkAt(new BlockPos((x + 1) * 16, 64, z * 16)),
				level.getChunkAt(new BlockPos((x - 1) * 16, 64, z * 16)),
				level.getChunkAt(new BlockPos(x * 16, 64, (z + 1) * 16)),
				level.getChunkAt(new BlockPos(x * 16, 64, (z - 1) * 16))
		);
	}

	@Override
	public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
		method.invoke(this, context);
	}
}
