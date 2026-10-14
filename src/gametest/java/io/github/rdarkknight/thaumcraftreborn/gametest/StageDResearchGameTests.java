package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.ResearchTableData;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.TheorycraftSystem;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class StageDResearchGameTests {
	@GameTest
	public void researchTableRequiresToolsAndPaperAndPersistsSession(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		BlockPos pos = context.absolutePos(new BlockPos(1, 1, 1));
		context.getLevel().setBlock(pos, ThaumcraftContent.RESEARCH_TABLE.defaultBlockState(), 3);
		ResearchTableBlockEntity table = (ResearchTableBlockEntity) context.getLevel().getBlockEntity(pos);
		table.setItem(0, new ItemStack(ThaumcraftContent.SCRIBING_TOOLS));
		context.assertTrue(!TheorycraftSystem.start(table, player), "session cannot start without paper");
		table.setItem(1, new ItemStack(Items.PAPER, 3));
		context.getLevel().setBlock(pos.east(), Blocks.BOOKSHELF.defaultBlockState(), 3);
		context.getLevel().setBlock(pos.west(), ThaumcraftContent.CRUCIBLE.defaultBlockState(), 3);

		context.assertTrue(TheorycraftSystem.start(table, player)
						&& table.data().inspirationStart() == 5
						&& table.data().inspiration() == 3
						&& table.data().aids().size() == 2,
				"bookshelf and Crucible aids each deduct one inspiration");
		context.assertTrue(TheorycraftSystem.availableCategories(player)
						.equals(List.of(ThaumcraftRebornApi.id("basics"))),
				"only BASICS is available before UNLOCKALCHEMY");
		context.assertTrue(TheorycraftSystem.draw(table, player, 2)
						&& table.getItem(1).getCount() == 2
						&& table.data().offeredCards().size() == 2,
				"a successful draw consumes one paper and persists two offers");
		var encoded = ResearchTableData.CODEC.encodeStart(JsonOps.INSTANCE, table.data()).result().orElseThrow();
		ResearchTableData restored = ResearchTableData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
		context.assertTrue(restored.inspiration() == table.data().inspiration()
						&& restored.offeredCards().equals(table.data().offeredCards())
						&& restored.aids().equals(table.data().aids()),
				"session offers, inspiration, and aids survive codec round-trip");
		context.succeed();
	}

	@GameTest
	public void advancingResearchChecksAndConsumesKnowledgeAndCraftFlags(GameTestHelper context) {
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		var baseAlchemy = ThaumcraftRebornApi.id("basealchemy");
		var metallurgy = ThaumcraftRebornApi.id("metallurgy");
		var alchemy = Optional.of(ThaumcraftRebornApi.id("alchemy"));
		KnowledgeAccess.get().addResearch(player, baseAlchemy);
		KnowledgeAccess.get().setResearchStage(player, baseAlchemy, 2);
		KnowledgeAccess.get().addResearch(player, metallurgy);
		KnowledgeAccess.get().setResearchStage(player, metallurgy, 1);
		KnowledgeAccess.get().addKnowledge(player, KnowledgeType.OBSERVATION, alchemy,
				KnowledgeType.OBSERVATION.progression());
		KnowledgeAccess.get().addKnowledge(player, KnowledgeType.THEORY, alchemy,
				KnowledgeType.THEORY.progression());
		CraftEventRecorder.record(player, new ItemStack(ThaumcraftContent.INGOT_BRASS));
		CraftEventRecorder.record(player, new ItemStack(ThaumcraftContent.INGOT_THAUMIUM));

		context.assertTrue(ResearchProgression.progressStage(player, metallurgy)
						&& KnowledgeAccess.get().knowledge(player).getKnowledgeRaw(KnowledgeType.OBSERVATION, alchemy) == 0
						&& KnowledgeAccess.get().knowledge(player).getResearchStage(metallurgy) == 2,
				"stage one validates its brass craft and spends Alchemy observation");
		context.assertTrue(ResearchProgression.progressStage(player, metallurgy)
						&& KnowledgeAccess.get().knowledge(player).getKnowledgeRaw(KnowledgeType.THEORY, alchemy) == 0
						&& KnowledgeAccess.get().knowledge(player).isResearchComplete(metallurgy),
				"stage two validates thaumium craft and spends Alchemy theory");
		context.succeed();
	}
}
