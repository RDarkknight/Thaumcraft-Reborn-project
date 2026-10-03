package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoader;

public final class DebugData {
	public static final SyncedDataLoader<DebugDataEntry> ENTRIES = new SyncedDataLoader<>(
			ThaumcraftRebornApi.id("debug_entries"),
			DebugDataEntry.CODEC,
			DebugDataEntry.STREAM_CODEC
	);

	private DebugData() {
	}

	public static void init() {
	}
}
