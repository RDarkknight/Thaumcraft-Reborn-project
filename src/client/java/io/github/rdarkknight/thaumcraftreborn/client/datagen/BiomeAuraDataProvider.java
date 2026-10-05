package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.BiomeAuraType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.registries.Registries;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;

final class BiomeAuraDataProvider implements DataProvider {
	private static final List<Definition> DEFINITIONS = List.of(
			new Definition("water", 0.33F, Aspects.WATER, ConventionalBiomeTags.IS_AQUATIC),
			new Definition("ocean", 0.33F, Aspects.WATER, ConventionalBiomeTags.IS_OCEAN),
			new Definition("river", 0.4F, Aspects.WATER, ConventionalBiomeTags.IS_RIVER),
			new Definition("wet", 0.4F, Aspects.WATER, ConventionalBiomeTags.IS_WET),
			new Definition("lush", 0.5F, Aspects.WATER, ConventionalBiomeTags.IS_LUSH),
			new Definition("hot", 0.33F, Aspects.FIRE, ConventionalBiomeTags.IS_HOT),
			new Definition("dry", 0.125F, Aspects.ENTROPY, ConventionalBiomeTags.IS_DRY),
			new Definition("nether", 0.125F, Aspects.FIRE, ConventionalBiomeTags.IS_NETHER),
			new Definition("mesa", 0.33F, Aspects.FIRE, ConventionalBiomeTags.IS_BADLANDS),
			new Definition("spooky", 0.5F, Aspects.FIRE, ConventionalBiomeTags.IS_SPOOKY),
			new Definition("dense", 0.4F, Aspects.ORDER, ConventionalBiomeTags.IS_VEGETATION_DENSE),
			new Definition("snowy", 0.25F, Aspects.ORDER, ConventionalBiomeTags.IS_SNOWY),
			new Definition("cold", 0.25F, Aspects.ORDER, ConventionalBiomeTags.IS_COLD),
			new Definition("mushroom", 0.75F, Aspects.ORDER, ConventionalBiomeTags.IS_MUSHROOM),
			new Definition("magical", 0.75F, Aspects.ORDER, ConventionalBiomeTags.IS_MAGICAL),
			new Definition("coniferous", 0.33F, Aspects.EARTH, ConventionalBiomeTags.IS_CONIFEROUS_TREE),
			new Definition("forest", 0.5F, Aspects.EARTH, ConventionalBiomeTags.IS_FOREST),
			new Definition("sandy", 0.25F, Aspects.EARTH, ConventionalBiomeTags.IS_SANDY),
			new Definition("beach", 0.3F, Aspects.EARTH, ConventionalBiomeTags.IS_BEACH),
			new Definition("jungle", 0.6F, Aspects.EARTH, ConventionalBiomeTags.IS_JUNGLE),
			new Definition("savanna", 0.25F, Aspects.AIR, ConventionalBiomeTags.IS_SAVANNA),
			new Definition("mountain", 0.3F, Aspects.AIR, ConventionalBiomeTags.IS_MOUNTAIN),
			new Definition("hills", 0.33F, Aspects.AIR, ConventionalBiomeTags.IS_HILL),
			new Definition("plains", 0.3F, Aspects.AIR, ConventionalBiomeTags.IS_PLAINS),
			new Definition("end", 0.125F, Aspects.AIR, ConventionalBiomeTags.IS_END),
			new Definition("sparse", 0.2F, Aspects.ENTROPY, ConventionalBiomeTags.IS_VEGETATION_SPARSE),
			new Definition("swamp", 0.5F, Aspects.ENTROPY, ConventionalBiomeTags.IS_SWAMP),
			new Definition("wasteland", 0.125F, Aspects.ENTROPY, ConventionalBiomeTags.IS_WASTELAND),
			new Definition("dead", 0.1F, Aspects.ENTROPY, ConventionalBiomeTags.IS_DEAD),
			new Definition("rare", null, null, ConventionalBiomeTags.IS_RARE),
			new Definition("void", null, null, ConventionalBiomeTags.IS_VOID)
	);

	private final PackOutput.PathProvider paths;

	BiomeAuraDataProvider(FabricPackOutput output) {
		this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "thaumcraft_reborn/biome_aura");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		CompletableFuture<?>[] writes = DEFINITIONS.stream()
				.map(definition -> {
					Identifier id = ThaumcraftRebornApi.id(definition.name());
					TagKey<Biome> tag = TagKey.create(
							Registries.BIOME,
							ThaumcraftRebornApi.id("aura_type/" + definition.name())
					);
					Optional<Float> modifier = Optional.ofNullable(definition.modifier());
					Optional<ResourceKey<Aspect>> aspect = Optional.ofNullable(definition.aspect());
					return DataProvider.saveStable(
							cachedOutput,
							BiomeAuraType.CODEC.encodeStart(
									JsonOps.INSTANCE,
									new BiomeAuraType(tag, modifier, aspect)
							).getOrThrow(),
							paths.json(id)
					);
				})
				.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(writes);
	}

	@Override
	public String getName() {
		return "Thaumcraft Reborn Biome Aura Types";
	}

	static List<Definition> definitions() {
		return DEFINITIONS;
	}

	record Definition(String name, Float modifier, ResourceKey<Aspect> aspect, TagKey<Biome> conventionalTag) {
	}
}
