package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.client.screen.ArcaneWorkbenchScreen;
import io.github.rdarkknight.thaumcraftreborn.client.screen.ResearchTableScreen;
import io.github.rdarkknight.thaumcraftreborn.client.ThaumonomiconScreen;
import io.github.rdarkknight.thaumcraftreborn.client.ThaumometerClient;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.crucible.CrucibleBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.network.AuraSyncPayload;
import io.github.rdarkknight.thaumcraftreborn.systems.ThaumometerSystem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;

public final class StageDClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft -> minecraft.player != null, 1200);
			BlockPos auraPos = context.computeOnClient(minecraft -> minecraft.player.blockPosition());
			context.waitFor(minecraft -> minecraft.level != null && minecraft.level.hasChunkAt(auraPos), 1200);
			BlockPos pos = context.computeOnClient(minecraft ->
					minecraft.player.blockPosition().relative(minecraft.player.getDirection(), 3));
			BlockPos workbenchPos = pos.offset(1, 0, 0);
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var level = player.level();
				player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
						new ItemStack(ThaumcraftContent.THAUMOMETER));
				level.setBlock(pos, ThaumcraftContent.CRUCIBLE.defaultBlockState(), 3);
				if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
					crucible.setWaterMb(1000);
				}
				level.setBlock(workbenchPos, ThaumcraftContent.ARCANE_WORKBENCH.defaultBlockState(), 3);
			});
			context.runOnClient(minecraft ->
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos).add(0.0, 0.3, 0.0))
			);
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(20);
			context.waitFor(minecraft -> minecraft.level != null
					&& minecraft.level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible
					&& crucible.waterMb() == CrucibleBlockEntity.CAPACITY, 1200);
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				player.level().getChunkAt(player.blockPosition()).setAttached(
						ModAttachments.AURA,
						new AuraChunkData((short) 100, 321.0F, 12.0F)
				);
				ThaumometerSystem.syncAura(player);
			});
			context.waitFor(minecraft -> {
				AuraSyncPayload aura = ThaumometerClient.cachedAura();
				return aura != null && aura.base() == 100 && aura.vis() == 321.0F && aura.flux() == 12.0F;
			}, 1200);
			context.takeScreenshot("thaumcraft_reborn-stage-d-crucible");
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				if (player.level().getBlockEntity(workbenchPos) instanceof ArcaneWorkbenchBlockEntity blockEntity) {
					player.openMenu(blockEntity);
				}
			});
			context.waitForScreen(ArcaneWorkbenchScreen.class);
			context.takeScreenshot("thaumcraft_reborn-stage-d-arcane-workbench");
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				player.closeContainer();
				BlockPos tablePos = player.blockPosition().offset(3, 2, 0);
				var level = player.level();
				level.setBlock(tablePos, ThaumcraftContent.RESEARCH_TABLE.defaultBlockState(), 3);
				if (level.getBlockEntity(tablePos) instanceof ResearchTableBlockEntity table) {
					table.setItem(0, new ItemStack(ThaumcraftContent.SCRIBING_TOOLS));
					table.setItem(1, new ItemStack(Items.PAPER, 2));
					player.openMenu(table);
				}
				KnowledgeAccess.get().addResearch(player, ThaumcraftRebornApi.id("firststeps"));
			});
			context.waitForScreen(ResearchTableScreen.class);
			context.takeScreenshot("thaumcraft_reborn-stage-d-research-table");
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				player.closeContainer();
				KnowledgeAccess.get().clearKnowledge(player);
				var book = new net.minecraft.world.entity.item.ItemEntity(player.level(),
						player.getX(), player.getY(), player.getZ(), new ItemStack(ThaumcraftContent.THAUMONOMICON));
				player.level().addFreshEntity(book);
				book.setNoPickUpDelay();
				book.playerTouch(player);
				if (!KnowledgeAccess.get().knowledge(player)
						.isResearchKnown(ThaumcraftRebornApi.id("flag/got_thaumonomicon"))) {
					throw new IllegalStateException("Thaumonomicon pickup did not grant its research flag");
				}
				player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
						new ItemStack(ThaumcraftContent.THAUMONOMICON));
			});
			context.runOnClient(minecraft -> minecraft.gui.setScreen(null));
			context.runOnClient(minecraft ->
					ThaumcraftContent.THAUMONOMICON.use(minecraft.level, minecraft.player, InteractionHand.MAIN_HAND));
			context.waitForScreen(ThaumonomiconScreen.class);
			context.waitFor(minecraft -> minecraft.gui.screen() instanceof ThaumonomiconScreen screen
					&& screen.hasVisibleEntry(ThaumcraftRebornApi.id("firststeps"))
					&& screen.selectEntry(ThaumcraftRebornApi.id("firststeps"))
					&& screen.isProgressButtonActive(), 1200);
			context.takeScreenshot("thaumcraft_reborn-stage-d-thaumonomicon");
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				if (!io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression
						.handleProgressRequest(player, ThaumcraftRebornApi.id("firststeps"))) {
					throw new IllegalStateException("First Steps request failed");
				}
				KnowledgeAccess.get().addKnowledge(player,
						io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType.THEORY,
						java.util.Optional.of(ThaumcraftRebornApi.id("alchemy")), 3);
			});
			context.waitFor(minecraft -> minecraft.player != null
							&& KnowledgeAccess.get().knowledge(minecraft.player)
									.isResearchKnown(ThaumcraftRebornApi.id("firststeps"))
								&& KnowledgeAccess.get().knowledge(minecraft.player).getKnowledgeRaw(
										io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType.THEORY,
										java.util.Optional.of(ThaumcraftRebornApi.id("alchemy"))) == 3,
					1200);
		}
	}
}
