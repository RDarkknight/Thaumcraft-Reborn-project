package io.github.rdarkknight.thaumcraftreborn.gametest;

import io.github.rdarkknight.thaumcraftreborn.client.network.ClientNetworkProbe;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class PhaseOneClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(minecraft -> {
				int sent = ClientNetworkProbe.lastSentNonce();
				return sent != Integer.MIN_VALUE && ClientNetworkProbe.lastReceivedNonce() == sent;
			}, 1200);

			BlockPos target = context.computeOnClient(minecraft ->
					minecraft.player.blockPosition().relative(minecraft.player.getDirection(), 3)
			);
			singleplayer.getServer().runOnServer(server ->
					server.overworld().setBlock(target, DebugContent.TEST_RENDER_BLOCK.defaultBlockState(), 3)
			);
			context.runOnClient(minecraft ->
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(target))
			);
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("thaumcraft_reborn-client-render-test");
		}
	}
}
