package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeAuraType(TagKey<Biome> biomes, Optional<Float> modifier, Optional<ResourceKey<Aspect>> aspect) {
	public static final Codec<BiomeAuraType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.BIOME).fieldOf("biomes").forGetter(BiomeAuraType::biomes),
			Codec.FLOAT.optionalFieldOf("modifier").forGetter(BiomeAuraType::modifier),
			ResourceKey.codec(ThaumcraftRegistryKeys.ASPECT).optionalFieldOf("aspect").forGetter(BiomeAuraType::aspect)
	).apply(instance, BiomeAuraType::new));
}
