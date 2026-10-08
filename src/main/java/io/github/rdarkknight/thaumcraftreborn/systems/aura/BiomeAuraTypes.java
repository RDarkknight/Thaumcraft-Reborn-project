package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import com.mojang.logging.LogUtils;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

public final class BiomeAuraTypes {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<BiomeCacheKey, Float> MODIFIER_CACHE = new ConcurrentHashMap<>();
	public static final SyncedDataLoader<BiomeAuraType> LOADER = new SyncedDataLoader<>(
			ThaumcraftRebornApi.id("biome_aura"),
			BiomeAuraType.CODEC,
			net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(BiomeAuraType.CODEC)
	);

	static {
		LOADER.addServerEntriesListener(MODIFIER_CACHE::clear);
		LOADER.addClientEntriesListener(MODIFIER_CACHE::clear);
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, clientSide) -> MODIFIER_CACHE.clear());
	}

	private BiomeAuraTypes() {
	}

	public static void init() {
	}

	public static float modifier(Holder<Biome> biome, boolean clientSide) {
		Optional<ResourceKey<Biome>> resourceKey = biome.unwrapKey();
		if (resourceKey.isEmpty()) {
			return calculate(biome, LOADER.entries(clientSide));
		}
		BiomeCacheKey cacheKey = new BiomeCacheKey(clientSide, resourceKey.get().identifier());
		return MODIFIER_CACHE.computeIfAbsent(cacheKey, ignored -> calculate(biome, LOADER.entries(clientSide)));
	}

	public static float modifier(Holder<Biome> biome, Map<Identifier, BiomeAuraType> types) {
		return calculate(biome, types);
	}

	private static float calculate(Holder<Biome> biome, Map<Identifier, BiomeAuraType> types) {
		float total = 0.0F;
		int count = 0;
		for (BiomeAuraType type : types.values()) {
			if (biome.is(type.biomes())) {
				if (type.modifier().isEmpty()) {
					LOGGER.debug("Biome {} matches aura type {} without a modifier; using the default", biome, type.biomes());
					return 0.5F;
				}
				total += type.modifier().get();
				count++;
			}
		}
		return count == 0 ? 0.5F : total / count;
	}

	private record BiomeCacheKey(boolean clientSide, Identifier biome) {
	}
}
