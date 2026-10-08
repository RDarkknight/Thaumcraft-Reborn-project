package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectMappings;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.BiomeAuraTypes;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchIndex;
import java.util.Optional;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.LevelChunk;

public final class StageBClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave savedWorld;
		BlockPos persistedChunkPos;
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft ->
					ResearchIndex.RESEARCH.clientEntries().containsKey(testId("fixture_main"))
							&& ResearchIndex.CATEGORIES.clientEntries().containsKey(ThaumcraftRebornApi.id("basics"))
							&& AspectMappings.LOADER.clientEntries().containsKey(testId("00_base"))
							&& BiomeAuraTypes.LOADER.clientEntries().containsKey(testId("test_primary"))
							&& loadersMatchServer(), 1200);

			int researchSyncsBeforeReload = ResearchIndex.RESEARCH.clientSyncCount();
			singleplayer.getServer().runCommand("reload");
			context.waitFor(minecraft -> ResearchIndex.RESEARCH.clientSyncCount() > researchSyncsBeforeReload, 1200);
			context.waitTicks(10);
			if (ResearchIndex.RESEARCH.clientSyncCount() != researchSyncsBeforeReload + 1
					|| !ResearchIndex.RESEARCH.clientEntries().containsKey(testId("fixture_main"))) {
				throw new AssertionError("research loader syncs exactly once after /reload and retains fixture entries");
			}

			singleplayer.getServer().runCommand(
					"thaumcraft_reborn research @a grant thaumcraft_reborn_test:fixture_main"
			);
			context.waitFor(minecraft -> minecraft.player != null
					&& KnowledgeAccess.get().knowledge(minecraft.player)
							.getResearchStatus(testId("fixture_main")) == ResearchStatus.COMPLETE
					&& KnowledgeAccess.get().warp(minecraft.player).get(WarpType.PERMANENT) == 2
					&& KnowledgeAccess.get().warp(minecraft.player).get(WarpType.NORMAL) == 2, 1200);
			context.waitTicks(10);

			persistedChunkPos = context.computeOnClient(minecraft ->
					minecraft.player.blockPosition().offset(5, 0, 5)
			);
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				LevelChunk chunk = player.level().getChunkAt(persistedChunkPos);
				chunk.setAttached(ModAttachments.AURA, new AuraChunkData((short) 42, 8.0F, 3.0F));
				KnowledgeAccess.get().setWarp(player, WarpType.TEMPORARY, 17);
				KnowledgeAccess.get().setWarpCounter(player, 9);
			});
			context.waitFor(minecraft -> minecraft.player != null
					&& KnowledgeAccess.get().warp(minecraft.player).get(WarpType.TEMPORARY) == 17
					&& KnowledgeAccess.get().warp(minecraft.player).getCounter() == 9, 1200);
			savedWorld = singleplayer.getWorldSave();
		}

		try (TestSingleplayerContext reopened = savedWorld.open()) {
			boolean restored = reopened.getServer().computeOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var knowledge = KnowledgeAccess.get().knowledge(player);
				var warp = KnowledgeAccess.get().warp(player);
				LevelChunk chunk = server.overworld().getChunkAt(persistedChunkPos);
				AuraChunkData aura = chunk.getAttached(ModAttachments.AURA);
				return knowledge.getResearchStatus(testId("fixture_main")) == ResearchStatus.COMPLETE
						&& knowledge.getKnowledgeRaw(KnowledgeType.THEORY,
								Optional.of(testId("fixture"))) == 64
						&& warp.get(WarpType.PERMANENT) == 2
						&& warp.get(WarpType.NORMAL) == 2
						&& warp.get(WarpType.TEMPORARY) == 17
						&& warp.getCounter() == 9
						&& aura != null
						&& aura.base() == 42
						&& aura.flux() == 3.0F;
			});
			if (!restored) {
				throw new AssertionError("aura, research, knowledge, and warp survive saving and reopening the world");
			}
		}
	}

	private static boolean loadersMatchServer() {
		return matchesServer(ResearchIndex.RESEARCH.clientEntries().size(), ResearchIndex.RESEARCH.serverEntries().size())
				&& matchesServer(ResearchIndex.CATEGORIES.clientEntries().size(), ResearchIndex.CATEGORIES.serverEntries().size())
				&& matchesServer(AspectMappings.LOADER.clientEntries().size(), AspectMappings.LOADER.serverEntries().size())
				&& matchesServer(BiomeAuraTypes.LOADER.clientEntries().size(), BiomeAuraTypes.LOADER.serverEntries().size());
	}

	private static boolean matchesServer(int clientEntries, int serverEntries) {
		return clientEntries == serverEntries;
	}

	private static Identifier testId(String path) {
		return Identifier.fromNamespaceAndPath("thaumcraft_reborn_test", path);
	}
}
