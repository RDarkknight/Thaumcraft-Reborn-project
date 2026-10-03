package io.github.rdarkknight.thaumcraftreborn.core.component;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;

public final class ModDataComponents {
	public static final DataComponentType<ProbeStamp> PROBE_STAMP = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ThaumcraftRebornApi.id("probe_stamp"),
			DataComponentType.<ProbeStamp>builder()
					.persistent(ProbeStamp.CODEC)
					.networkSynchronized(ProbeStamp.STREAM_CODEC)
					.build()
	);

	private ModDataComponents() {
	}

	public static void init() {
	}
}
