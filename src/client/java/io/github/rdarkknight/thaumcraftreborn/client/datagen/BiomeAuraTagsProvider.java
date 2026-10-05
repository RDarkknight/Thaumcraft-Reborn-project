package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

final class BiomeAuraTagsProvider extends FabricTagsProvider<Biome> {
	BiomeAuraTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(output, Registries.BIOME, registryLookup);
	}

	@Override
	protected void addTags(HolderLookup.Provider registryLookup) {
		for (BiomeAuraDataProvider.Definition definition : BiomeAuraDataProvider.definitions()) {
			TagKey<Biome> tag = TagKey.create(
					Registries.BIOME,
					ThaumcraftRebornApi.id("aura_type/" + definition.name())
			);
			tag(tag).addOptionalTag(definition.conventionalTag());
		}
	}
}
