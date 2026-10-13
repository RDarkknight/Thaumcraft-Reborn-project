package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.client.screen.ArcaneWorkbenchScreen;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchBlockEntity;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

public final class StageDClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft -> minecraft.player != null, 1200);
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				BlockPos pos = player.blockPosition().offset(3, 0, 0);
				var level = player.level();
				level.setBlock(pos, ThaumcraftContent.ARCANE_WORKBENCH.defaultBlockState(), 3);
				if (level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity blockEntity) {
					player.openMenu(blockEntity);
				}
			});
			context.waitForScreen(ArcaneWorkbenchScreen.class);
			context.takeScreenshot("thaumcraft_reborn-stage-d-arcane-workbench");
		}
	}
}
