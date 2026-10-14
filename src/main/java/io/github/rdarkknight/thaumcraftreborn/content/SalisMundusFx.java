package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;

public final class SalisMundusFx {
	public static final FxType<Integer> SPARK_BURST = Registry.register(
			ThaumcraftRegistries.FX_TYPE,
			ThaumcraftRebornApi.id("salis_mundus_spark_burst"),
			new FxType<>(ByteBufCodecs.VAR_INT.cast())
	);

	private SalisMundusFx() {
	}

	public static void init() {
	}
}
