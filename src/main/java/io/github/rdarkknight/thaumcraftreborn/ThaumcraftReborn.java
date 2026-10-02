package io.github.rdarkknight.thaumcraftreborn;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.compat.CompatBootstrap;
import io.github.rdarkknight.thaumcraftreborn.content.ContentBootstrap;
import io.github.rdarkknight.thaumcraftreborn.core.CoreBootstrap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThaumcraftReborn implements ModInitializer {
	public static final String MOD_ID = ThaumcraftRebornApi.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		CoreBootstrap.init();
		ContentBootstrap.init();
		CompatBootstrap.init();
		LOGGER.info("Thaumcraft Reborn Phase 1 initialized");
	}
}
