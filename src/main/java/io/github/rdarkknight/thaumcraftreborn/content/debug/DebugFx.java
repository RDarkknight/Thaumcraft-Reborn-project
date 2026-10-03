package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class DebugFx {
	public static final SimpleParticleType DEBUG_SPARK = Registry.register(
			BuiltInRegistries.PARTICLE_TYPE,
			ThaumcraftRebornApi.id("debug_spark"),
			FabricParticleTypes.simple()
	);
	public static final FxType<Integer> DEBUG_BURST = Registry.register(
			ThaumcraftRegistries.FX_TYPE,
			ThaumcraftRebornApi.id("debug_burst"),
			new FxType<>(ByteBufCodecs.VAR_INT.cast())
	);

	private DebugFx() {
	}

	public static void init() {
	}
}
