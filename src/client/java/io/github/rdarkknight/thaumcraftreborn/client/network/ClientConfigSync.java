package io.github.rdarkknight.thaumcraftreborn.client.network;

import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.network.ConfigSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientConfigSync {
	private ClientConfigSync() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ThaumcraftConfig.setSynchronizedCommon(payload.config())));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ThaumcraftConfig.clearSynchronizedCommon());
	}
}
