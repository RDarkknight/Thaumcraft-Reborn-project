package io.github.rdarkknight.thaumcraftreborn.core.component;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
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
	public static final DataComponentType<AspectList> ASPECTS = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ThaumcraftRebornApi.id("aspects"),
			DataComponentType.<AspectList>builder()
					.persistent(AspectList.CODEC)
					.networkSynchronized(AspectList.STREAM_CODEC)
					.build()
	);
	public static final DataComponentType<Aspect> CRYSTAL_ASPECT = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ThaumcraftRebornApi.id("crystal_aspect"),
			DataComponentType.<Aspect>builder()
					.persistent(Aspect.CODEC)
					.networkSynchronized(Aspect.STREAM_CODEC)
					.build()
	);

	private ModDataComponents() {
	}

	public static void init() {
	}
}
