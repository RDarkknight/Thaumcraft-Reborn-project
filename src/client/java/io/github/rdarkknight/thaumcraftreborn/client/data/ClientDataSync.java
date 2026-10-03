package io.github.rdarkknight.thaumcraftreborn.client.data;

import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoader;
import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoaders;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientDataSync {
	private ClientDataSync() {
	}

	public static void init() {
		for (SyncedDataLoader<?> loader : SyncedDataLoaders.all()) {
			register(loader);
		}

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
				SyncedDataLoaders.all().forEach(SyncedDataLoader::clearClientEntries));
	}

	private static <T> void register(SyncedDataLoader<T> loader) {
			ClientPlayNetworking.registerGlobalReceiver(loader.payloadType(), (payload, context) ->
				context.client().execute(() -> loader.replaceClientEntries(payload.entries())));
	}
}
