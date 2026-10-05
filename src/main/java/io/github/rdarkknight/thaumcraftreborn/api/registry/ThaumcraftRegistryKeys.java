package io.github.rdarkknight.thaumcraftreborn.api.registry;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class ThaumcraftRegistryKeys {
	public static final ResourceKey<Registry<Aspect>> ASPECT = ResourceKey.createRegistryKey(ThaumcraftRebornApi.id("aspect"));
	public static final ResourceKey<Registry<FxType<?>>> FX_TYPE = ResourceKey.createRegistryKey(ThaumcraftRebornApi.id("fx_type"));

	private ThaumcraftRegistryKeys() {
	}
}
