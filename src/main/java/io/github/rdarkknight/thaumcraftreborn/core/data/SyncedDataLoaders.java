package io.github.rdarkknight.thaumcraftreborn.core.data;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class SyncedDataLoaders {
	private static final List<SyncedDataLoader<?>> LOADERS = new ArrayList<>();

	private SyncedDataLoaders() {
	}

	static void register(SyncedDataLoader<?> loader) {
		LOADERS.add(loader);
		registerPayload(loader);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> loader.syncTo(player));
	}

	private static <T> void registerPayload(SyncedDataLoader<T> loader) {
		PayloadTypeRegistry.clientboundPlay().register(loader.payloadType(), loader.payloadCodec());
	}

	public static List<SyncedDataLoader<?>> all() {
		return List.copyOf(LOADERS);
	}
}
