package io.github.rdarkknight.thaumcraftreborn.client;

import io.github.rdarkknight.thaumcraftreborn.client.network.ClientNetworkProbe;
import io.github.rdarkknight.thaumcraftreborn.client.render.TestRenderBlockEntityRenderer;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ThaumcraftRebornClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(DebugContent.TEST_RENDER_BLOCK_ENTITY, TestRenderBlockEntityRenderer::new);
		ClientNetworkProbe.init();
	}
}
