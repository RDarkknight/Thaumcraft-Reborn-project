package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import io.github.rdarkknight.thaumcraftreborn.client.ThaumonomiconScreen;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class StageC2ClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft -> minecraft.player != null, 1200);
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var firststeps = ThaumcraftRebornApi.id("firststeps");
				KnowledgeAccess.get().addResearch(player, firststeps);
				KnowledgeAccess.get().setResearchStage(player, firststeps, 3);
				player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ThaumcraftContent.GOGGLES));
				player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ThaumcraftContent.THAUMOMETER));
			});
			var basics = ThaumcraftRebornApi.id("basics");
			var firststeps = ThaumcraftRebornApi.id("firststeps");
			context.waitFor(minecraft -> minecraft.player != null
					&& ResearchAccess.get().category(basics, true).isPresent()
					&& ResearchAccess.get().entriesInCategory(basics, true).contains(firststeps)
					&& KnowledgeAccess.get().knowledge(minecraft.player).isResearchKnown(firststeps)
					&& KnowledgeAccess.get().knowledge(minecraft.player).getResearchStatus(firststeps) != ResearchStatus.UNKNOWN, 1200);
			context.setScreen(ThaumonomiconScreen::new);
			context.waitForScreen(ThaumonomiconScreen.class);
			context.takeScreenshot("thaumcraft_reborn-c2-thaumonomicon");
			context.runOnClient(minecraft -> minecraft.gui.setScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("thaumcraft_reborn-c2-goggles-hud");
		}
	}
}
