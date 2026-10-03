package io.github.rdarkknight.thaumcraftreborn.gametest;

import com.google.gson.JsonParser;
import io.github.rdarkknight.thaumcraftreborn.client.config.ClientConfig;
import io.github.rdarkknight.thaumcraftreborn.client.fx.DebugBurstHandler;
import io.github.rdarkknight.thaumcraftreborn.client.screen.DebugContainerScreen;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugData;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import io.github.rdarkknight.thaumcraftreborn.core.config.CommonConfig;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxDispatcher;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

public final class StageAClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft ->
					DebugData.ENTRIES.clientEntries().containsKey(
							io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi.id("sample")
					), 1200);
			context.waitFor(minecraft ->
					ThaumcraftConfig.hasSynchronizedCommon()
							&& ThaumcraftConfig.common(true).equals(ThaumcraftConfig.common(false)), 1200);

			ClientConfig clientDefaults = ClientConfig.CODEC.parse(
					com.mojang.serialization.JsonOps.INSTANCE,
					JsonParser.parseString("{}")
			).getOrThrow();
			if (!clientDefaults.equals(ClientConfig.DEFAULT)) {
				throw new AssertionError("empty client config uses defaults");
			}

			BlockPos target = context.computeOnClient(minecraft ->
					minecraft.player.blockPosition().relative(minecraft.player.getDirection(), 3)
			);
			DebugBurstHandler.resetExecutionCount();
			singleplayer.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var level = player.level();
				level.setBlock(target, DebugContent.DEBUG_CONTAINER.defaultBlockState(), 3);
				if (level.getBlockEntity(target) instanceof DebugContainerBlockEntity blockEntity) {
					player.openMenu(blockEntity);
					FxDispatcher.send(level, player.position(), DebugFx.DEBUG_BURST, 12);
				}
			});

			context.waitFor(minecraft ->
					minecraft.player != null
							&& minecraft.gui.screen() instanceof DebugContainerScreen
							&& minecraft.player.containerMenu instanceof DebugContainerMenu menu
							&& menu.ticks() > 0, 1200);
			context.waitFor(minecraft -> DebugBurstHandler.executionCount() > 0, 1200);
			context.waitTicks(10);
			context.takeScreenshot("thaumcraft_reborn-stage-a-debug-container");
		}
	}
}
