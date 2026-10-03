package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class ThaumcraftRebornDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(ModModelProvider::new);
		pack.addProvider((output, lookup) -> new EnglishLanguageProvider(output, lookup));
		pack.addProvider((output, lookup) -> new SpanishLanguageProvider(output, lookup));
		pack.addProvider(ModBlockLootProvider::new);
	}

	private static final class ModModelProvider implements DataProvider {
		private final PackOutput.PathProvider blockStatePaths;
		private final PackOutput.PathProvider blockModelPaths;
		private final PackOutput.PathProvider itemModelPaths;
		private final PackOutput.PathProvider itemDefinitionPaths;

		private ModModelProvider(FabricPackOutput output) {
			this.blockStatePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
			this.blockModelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
			this.itemModelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
			this.itemDefinitionPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
		}

		@Override
		public CompletableFuture<?> run(CachedOutput cachedOutput) {
			Identifier blockId = Identifier.fromNamespaceAndPath(ThaumcraftReborn.MOD_ID, "test_render_block");
			Identifier debugContainerId = Identifier.fromNamespaceAndPath(ThaumcraftReborn.MOD_ID, "debug_container");
			Identifier probeId = Identifier.fromNamespaceAndPath(ThaumcraftReborn.MOD_ID, "test_probe");

			JsonObject blockState = new JsonObject();
			JsonObject variants = new JsonObject();
			JsonArray defaultVariant = new JsonArray();
			JsonObject blockVariant = new JsonObject();
			blockVariant.addProperty("model", ThaumcraftReborn.MOD_ID + ":block/test_render_block");
			defaultVariant.add(blockVariant);
			variants.add("", defaultVariant);
			blockState.add("variants", variants);

			JsonObject blockModel = new JsonObject();
			blockModel.addProperty("parent", "minecraft:block/cube_all");
			JsonObject blockTextures = new JsonObject();
			blockTextures.addProperty("all", ThaumcraftReborn.MOD_ID + ":block/test_render_block");
			blockModel.add("textures", blockTextures);

			JsonObject debugContainerBlockState = new JsonObject();
			JsonObject debugContainerVariants = new JsonObject();
			JsonArray debugContainerDefaultVariant = new JsonArray();
			JsonObject debugContainerVariant = new JsonObject();
			debugContainerVariant.addProperty("model", ThaumcraftReborn.MOD_ID + ":block/debug_container");
			debugContainerDefaultVariant.add(debugContainerVariant);
			debugContainerVariants.add("", debugContainerDefaultVariant);
			debugContainerBlockState.add("variants", debugContainerVariants);

			JsonObject debugContainerModel = new JsonObject();
			debugContainerModel.addProperty("parent", "minecraft:block/cube_all");
			JsonObject debugContainerTextures = new JsonObject();
			debugContainerTextures.addProperty("all", ThaumcraftReborn.MOD_ID + ":block/debug_container");
			debugContainerModel.add("textures", debugContainerTextures);

			JsonObject probeItemModel = new JsonObject();
			probeItemModel.addProperty("parent", "minecraft:item/generated");
			JsonObject probeTextures = new JsonObject();
			probeTextures.addProperty("layer0", ThaumcraftReborn.MOD_ID + ":item/test_probe");
			probeItemModel.add("textures", probeTextures);

			return CompletableFuture.allOf(
					DataProvider.saveStable(cachedOutput, blockState, this.blockStatePaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, blockModel, this.blockModelPaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, debugContainerBlockState, this.blockStatePaths.json(debugContainerId)),
					DataProvider.saveStable(cachedOutput, debugContainerModel, this.blockModelPaths.json(debugContainerId)),
					DataProvider.saveStable(cachedOutput, probeItemModel, this.itemModelPaths.json(probeId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("item/test_probe"), this.itemDefinitionPaths.json(probeId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("block/test_render_block"), this.itemDefinitionPaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("block/debug_container"), this.itemDefinitionPaths.json(debugContainerId))
			);
		}

		@Override
		public String getName() {
			return "Thaumcraft Reborn Models";
		}

		private static JsonObject itemDefinition(String model) {
			JsonObject itemDefinition = new JsonObject();
			JsonObject modelDefinition = new JsonObject();
			modelDefinition.addProperty("type", "minecraft:model");
			modelDefinition.addProperty("model", ThaumcraftReborn.MOD_ID + ":" + model);
			itemDefinition.add("model", modelDefinition);
			return itemDefinition;
		}
	}

	private static final class EnglishLanguageProvider extends FabricLanguageProvider {
		private EnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translations) {
			addTranslations(translations, "Test Probe", "Test Render Block", "Debug Container");
		}
	}

	private static final class SpanishLanguageProvider extends FabricLanguageProvider {
		private SpanishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, "es_es", registryLookup);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translations) {
			addTranslations(translations, "Sonda de prueba", "Bloque de prueba de renderizado", "Contenedor de depuración");
		}
	}

	private static void addTranslations(
			FabricLanguageProvider.TranslationBuilder translations,
			String probeName,
			String blockName,
			String debugContainerName
	) {
		translations.add("item.thaumcraft_reborn.test_probe", probeName);
		translations.add("block.thaumcraft_reborn.test_render_block", blockName);
		translations.add("block.thaumcraft_reborn.debug_container", debugContainerName);
		translations.add("container.thaumcraft_reborn.debug_container", debugContainerName);
	}

	private static final class ModBlockLootProvider extends FabricBlockLootSubProvider {
		private ModBlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			dropSelf(DebugContent.TEST_RENDER_BLOCK);
			dropSelf(DebugContent.DEBUG_CONTAINER);
		}
	}
}
