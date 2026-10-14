package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import java.util.HashSet;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ThaumonomiconResearchGameTests {
	private static final net.minecraft.resources.Identifier FIRST_STEPS = ThaumcraftRebornApi.id("firststeps");
	private static final net.minecraft.resources.Identifier GOT_THAUMONOMICON =
			ThaumcraftRebornApi.id("flag/got_thaumonomicon");
	private static final net.minecraft.resources.Identifier BASE_ALCHEMY = ThaumcraftRebornApi.id("basealchemy");
	private static final net.minecraft.resources.Identifier FLUX = ThaumcraftRebornApi.id("flux");

	@GameTest
	public void browserVisibilityRespectsParentsAndHiddenRoots(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		KnowledgeAccess.get().clearKnowledge(player);
		KnowledgeAccess.get().addResearch(player, GOT_THAUMONOMICON);
		context.assertTrue(ResearchProgression.isVisible(player, FIRST_STEPS, new HashSet<>()),
				"First Steps is visible after obtaining the Thaumonomicon");
		context.assertFalse(ResearchProgression.isVisible(player, FLUX, new HashSet<>()),
				"a hidden entry without parents remains invisible");
		context.assertFalse(ResearchProgression.isVisible(player, BASE_ALCHEMY, new HashSet<>()),
				"an entry with an unknown parent remains invisible");
		context.succeed();
	}

	@GameTest
	public void progressRequestStartsResearchAndRejectsMissingParents(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		KnowledgeAccess.get().addResearch(player, GOT_THAUMONOMICON);
		player.getInventory().setItem(0, new ItemStack(Items.PAPER, 2));
		context.assertTrue(ResearchProgression.handleProgressRequest(player, FIRST_STEPS),
				"a valid request starts First Steps");
		context.assertTrue(KnowledgeAccess.get().knowledge(player).isResearchKnown(FIRST_STEPS)
						&& KnowledgeAccess.get().knowledge(player).getResearchStage(FIRST_STEPS) == 1,
				"starting research records its first stage");
		context.assertTrue(KnowledgeAccess.get().knowledge(player)
						.getKnowledge(KnowledgeType.OBSERVATION, Optional.empty()) == 0,
				"starting research does not charge knowledge");
		context.assertTrue(player.getInventory().getItem(0).getCount() == 2,
				"starting research does not consume inventory items");
		context.assertFalse(ResearchProgression.handleProgressRequest(player, BASE_ALCHEMY),
				"a request cannot start research whose parent is unknown");
		context.assertFalse(KnowledgeAccess.get().knowledge(player).isResearchKnown(BASE_ALCHEMY),
				"rejected research remains unknown");
		context.assertFalse(ResearchProgression.handleProgressRequest(player, ThaumcraftRebornApi.id("missing_entry")),
				"an unregistered research key is rejected");
		context.succeed();
	}
}
