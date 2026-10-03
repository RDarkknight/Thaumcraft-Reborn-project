package io.github.rdarkknight.thaumcraftreborn.core.config;

import io.github.rdarkknight.thaumcraftreborn.core.network.ConfigSyncPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class ThaumcraftConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static volatile CommonConfig common = CommonConfig.DEFAULT;
	private static volatile CommonConfig synchronizedCommon;

	private ThaumcraftConfig() {
	}

	public static void init() {
		reloadCommon();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> reloadCommon());
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
				ServerPlayNetworking.send(player, new ConfigSyncPayload(common(false))));
	}

	public static CommonConfig common(boolean clientSide) {
		CommonConfig synchronizedConfig = synchronizedCommon;
		return clientSide && synchronizedConfig != null ? synchronizedConfig : common;
	}

	public static boolean hasSynchronizedCommon() {
		return synchronizedCommon != null;
	}

	public static void setSynchronizedCommon(CommonConfig config) {
		synchronizedCommon = config;
	}

	public static void clearSynchronizedCommon() {
		synchronizedCommon = null;
	}

	private static void reloadCommon() {
		common = ConfigFiles.load(configPath("common.json"), CommonConfig.CODEC, CommonConfig.DEFAULT);
		LOGGER.info("Loaded common configuration");
	}

	private static java.nio.file.Path configPath(String filename) {
		return FabricLoader.getInstance().getConfigDir().resolve("thaumcraft_reborn").resolve(filename);
	}
}
