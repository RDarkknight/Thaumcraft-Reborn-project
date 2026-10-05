package io.github.rdarkknight.thaumcraftreborn.core.registry;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class ThaumcraftRegistries {
	public static final Registry<Aspect> ASPECT = createSynced(ThaumcraftRegistryKeys.ASPECT);
	public static final Registry<FxType<?>> FX_TYPE = createSynced(ThaumcraftRegistryKeys.FX_TYPE);

	private ThaumcraftRegistries() {
	}

	public static <T> Registry<T> createSynced(ResourceKey<Registry<T>> key) {
		return FabricRegistryBuilder.create(key).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}
}
