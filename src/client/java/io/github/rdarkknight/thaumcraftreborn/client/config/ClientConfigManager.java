package io.github.rdarkknight.thaumcraftreborn.client.config;

import io.github.rdarkknight.thaumcraftreborn.core.config.ConfigFiles;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfigManager {
	private static volatile ClientConfig config = ClientConfig.DEFAULT;

	private ClientConfigManager() {
	}

	public static void init() {
		config = ConfigFiles.load(
				FabricLoader.getInstance().getConfigDir().resolve("thaumcraft_reborn").resolve("client.json"),
				ClientConfig.CODEC,
				ClientConfig.DEFAULT
		);
	}

	public static ClientConfig get() {
		return config;
	}
}
