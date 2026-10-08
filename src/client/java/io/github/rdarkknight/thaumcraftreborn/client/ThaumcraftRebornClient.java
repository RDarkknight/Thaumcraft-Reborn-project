package io.github.rdarkknight.thaumcraftreborn.client;

import io.github.rdarkknight.thaumcraftreborn.client.network.ClientNetworkProbe;
import io.github.rdarkknight.thaumcraftreborn.client.config.ClientConfigManager;
import io.github.rdarkknight.thaumcraftreborn.client.data.ClientDataSync;
import io.github.rdarkknight.thaumcraftreborn.client.fx.FxHandlers;
import io.github.rdarkknight.thaumcraftreborn.client.particle.DebugParticles;
import io.github.rdarkknight.thaumcraftreborn.client.network.ClientConfigSync;
import io.github.rdarkknight.thaumcraftreborn.client.render.TestRenderBlockEntityRenderer;
import io.github.rdarkknight.thaumcraftreborn.client.screen.DebugContainerScreen;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ThaumcraftRebornClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientConfigManager.init();
		ClientConfigSync.init();
		ClientDataSync.init();
		ThaumometerClient.init();
		FxHandlers.init();
		DebugParticles.register();
		MenuScreens.register(DebugContainerMenu.TYPE, DebugContainerScreen::new);
		BlockEntityRenderers.register(DebugContent.TEST_RENDER_BLOCK_ENTITY, TestRenderBlockEntityRenderer::new);
		ClientNetworkProbe.init();
	}
}
