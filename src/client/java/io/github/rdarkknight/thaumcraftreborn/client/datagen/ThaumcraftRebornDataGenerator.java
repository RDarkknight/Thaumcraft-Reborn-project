package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.PrimalCrystalBlock;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectDefinitions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

public final class ThaumcraftRebornDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(ModModelProvider::new);
		pack.addProvider(PlaceholderTextureProvider::new);
		pack.addProvider(StageCRecipeDataProvider::new);
		pack.addProvider((output, lookup) -> new EnglishLanguageProvider(output, lookup));
		pack.addProvider((output, lookup) -> new SpanishLanguageProvider(output, lookup));
		pack.addProvider(ModBlockLootProvider::new);
		pack.addProvider(BiomeAuraDataProvider::new);
		pack.addProvider((output, lookup) -> new BiomeAuraTagsProvider(output, lookup));
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

			List<CompletableFuture<?>> generated = new ArrayList<>(List.of(
					DataProvider.saveStable(cachedOutput, blockState, this.blockStatePaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, blockModel, this.blockModelPaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, debugContainerBlockState, this.blockStatePaths.json(debugContainerId)),
					DataProvider.saveStable(cachedOutput, debugContainerModel, this.blockModelPaths.json(debugContainerId)),
					DataProvider.saveStable(cachedOutput, probeItemModel, this.itemModelPaths.json(probeId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("item/test_probe"), this.itemDefinitionPaths.json(probeId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("block/test_render_block"), this.itemDefinitionPaths.json(blockId)),
					DataProvider.saveStable(cachedOutput, itemDefinition("block/debug_container"), this.itemDefinitionPaths.json(debugContainerId))
			));
			for (Block block : ThaumcraftContent.BLOCKS) {
				Identifier id = BuiltInRegistries.BLOCK.getKey(block);
				generated.add(DataProvider.saveStable(cachedOutput, blockState(id), this.blockStatePaths.json(id)));
				generated.add(DataProvider.saveStable(cachedOutput, blockModel(id), this.blockModelPaths.json(id)));
				generated.add(DataProvider.saveStable(cachedOutput, itemDefinition("block/" + id.getPath()),
						this.itemDefinitionPaths.json(id)));
			}
			for (Item item : ThaumcraftContent.ITEMS) {
				Identifier id = BuiltInRegistries.ITEM.getKey(item);
				JsonObject model = new JsonObject();
				model.addProperty("parent", "minecraft:item/generated");
				JsonObject textures = new JsonObject();
				textures.addProperty("layer0", ThaumcraftReborn.MOD_ID + ":item/" + id.getPath());
				model.add("textures", textures);
				generated.add(DataProvider.saveStable(cachedOutput, model, this.itemModelPaths.json(id)));
				generated.add(DataProvider.saveStable(cachedOutput, itemDefinition("item/" + id.getPath()),
						this.itemDefinitionPaths.json(id)));
			}
			Identifier essenceId = BuiltInRegistries.ITEM.getKey(ThaumcraftContent.CRYSTAL_ESSENCE);
			JsonObject essenceModel = new JsonObject();
			essenceModel.addProperty("parent", "minecraft:item/generated");
			JsonObject essenceTextures = new JsonObject();
			essenceTextures.addProperty("layer0", ThaumcraftReborn.MOD_ID + ":item/" + essenceId.getPath());
			essenceModel.add("textures", essenceTextures);
			generated.add(DataProvider.saveStable(cachedOutput, essenceModel, this.itemModelPaths.json(essenceId)));
			generated.add(DataProvider.saveStable(cachedOutput, itemDefinition("item/" + essenceId.getPath()),
					this.itemDefinitionPaths.json(essenceId)));
			return CompletableFuture.allOf(generated.toArray(CompletableFuture[]::new));
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

		private static JsonObject blockState(Identifier id) {
			JsonObject state = new JsonObject();
			JsonObject variants = new JsonObject();
			if (BuiltInRegistries.BLOCK.getValue(id) instanceof PrimalCrystalBlock) {
				for (int size = 0; size <= 3; size++) {
					for (int generation = 1; generation <= 4; generation++) {
						JsonArray crystalVariant = new JsonArray();
						JsonObject blockVariant = new JsonObject();
						blockVariant.addProperty("model", ThaumcraftReborn.MOD_ID + ":block/" + id.getPath());
						crystalVariant.add(blockVariant);
						variants.add("size=" + size + ",gen=" + generation, crystalVariant);
					}
				}
			} else {
				JsonArray defaultVariant = new JsonArray();
				JsonObject blockVariant = new JsonObject();
				blockVariant.addProperty("model", ThaumcraftReborn.MOD_ID + ":block/" + id.getPath());
				defaultVariant.add(blockVariant);
				variants.add("", defaultVariant);
			}
			state.add("variants", variants);
			return state;
		}

		private static JsonObject blockModel(Identifier id) {
			JsonObject model = new JsonObject();
			model.addProperty("parent", "minecraft:block/cube_all");
			JsonObject textures = new JsonObject();
			textures.addProperty("all", ThaumcraftReborn.MOD_ID + ":block/" + id.getPath());
			model.add("textures", textures);
			return model;
		}
	}

	private static final class EnglishLanguageProvider extends FabricLanguageProvider {
		private EnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translations) {
			addTranslations(translations, "Test Probe", "Test Render Block", "Debug Container");
			addContentTranslations(translations, false);
			addAspectTranslations(translations);
			addC2Translations(translations, false);
		}
	}

	private static final class SpanishLanguageProvider extends FabricLanguageProvider {
		private SpanishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, "es_es", registryLookup);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translations) {
			addTranslations(translations, "Sonda de prueba", "Bloque de prueba de renderizado", "Contenedor de depuración");
			addContentTranslations(translations, true);
			addAspectTranslations(translations);
			addC2Translations(translations, true);
		}
	}

	private static void addC2Translations(FabricLanguageProvider.TranslationBuilder translations, boolean spanish) {
		translations.add("message.thaumcraft_reborn.scan_found", spanish ? "Nuevo descubrimiento" : "New discovery");
		translations.add("message.thaumcraft_reborn.scan_known", spanish ? "Ya conocido" : "Already known");
		translations.add("message.thaumcraft_reborn.scan_unknown", spanish ? "Sin aspectos que descubrir" : "Nothing to discover");
		translations.add("message.thaumcraft_reborn.flux_warning", spanish ? "El flujo supera al vis" : "Flux exceeds vis");
		translations.add("gui.thaumcraft_reborn.research.categories", spanish ? "Categorías" : "Categories");
		translations.add("gui.thaumcraft_reborn.research.entries", spanish ? "Investigaciones" : "Research");
		translations.add("gui.thaumcraft_reborn.research.stages", spanish ? "Etapas conocidas" : "Known stages");
		translations.add("research.thaumcraft_reborn.firststeps.title", spanish ? "Primeros pasos" : "First Steps");
		translations.add("research.thaumcraft_reborn.firststeps.stage.1", spanish ? "El primer registro." : "The first entry.");
		translations.add("research.thaumcraft_reborn.firststeps.stage.2", spanish ? "Observa el mundo." : "Observe the world.");
		translations.add("research.thaumcraft_reborn.firststeps.stage.3", spanish ? "Listo para continuar." : "Ready to continue.");
		translations.add("hud.thaumcraft_reborn.revealing", spanish ? "Revelación" : "Revealing");
		translations.add("hud.thaumcraft_reborn.vis", spanish ? "Vis" : "Vis");
		translations.add("hud.thaumcraft_reborn.flux", spanish ? "Flujo" : "Flux");
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

	private static void addAspectTranslations(FabricLanguageProvider.TranslationBuilder translations) {
		for (Aspect aspect : AspectDefinitions.ALL) {
			String tag = aspect.tag();
			translations.add(aspect.translationKey(), Character.toUpperCase(tag.charAt(0)) + tag.substring(1));
		}
	}

	private static void addContentTranslations(FabricLanguageProvider.TranslationBuilder translations, boolean spanish) {
		for (Block block : ThaumcraftContent.BLOCKS) {
			String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
			translations.add("block.thaumcraft_reborn." + path, displayName(path, spanish));
		}
		for (Item item : ThaumcraftContent.ITEMS) {
			String path = BuiltInRegistries.ITEM.getKey(item).getPath();
			translations.add("item.thaumcraft_reborn." + path, displayName(path, spanish));
		}
	}

	private static String displayName(String path, boolean spanish) {
		if (spanish) {
			return switch (path) {
				case "ore_amber" -> "Mineral de ámbar";
				case "ore_cinnabar" -> "Mineral de cinabrio";
				case "ore_quartz" -> "Mineral de cuarzo";
				case "stone_arcane" -> "Piedra arcana";
				case "stone_arcane_brick" -> "Ladrillos de piedra arcana";
				case "amber_block" -> "Bloque de ámbar";
				case "amber_brick" -> "Ladrillo de ámbar";
				case "metal_thaumium" -> "Bloque de taumio";
				case "ingot_thaumium" -> "Lingote de taumio";
				case "nugget_thaumium" -> "Pepita de taumio";
				case "nugget_quartz" -> "Pepita de cuarzo";
				case "nugget_quicksilver" -> "Pepita de azogue";
				case "thaumometer" -> "Thaumómetro";
				case "goggles" -> "Gafas de revelación";
				case "thaumonomicon_normal" -> "Thaumonomicon";
				case "quicksilver" -> "Azogue";
				case "log_greatwood" -> "Tronco de madera excelsa";
				case "log_silverwood" -> "Tronco de madera plateada";
				case "plank_greatwood" -> "Tablones de madera excelsa";
				case "plank_silverwood" -> "Tablones de madera plateada";
				case "leaves_greatwood" -> "Hojas de madera excelsa";
				case "leaves_silverwood" -> "Hojas de madera plateada";
				case "sapling_greatwood" -> "Brote de madera excelsa";
				case "sapling_silverwood" -> "Brote de madera plateada";
				case "thaumium_sword" -> "Espada de taumio";
				case "thaumium_axe" -> "Hacha de taumio";
				case "thaumium_pick" -> "Pico de taumio";
				case "thaumium_shovel" -> "Pala de taumio";
				case "thaumium_hoe" -> "Azada de taumio";
				default -> path.startsWith("crystal_")
						? "Cristal de " + path.substring("crystal_".length())
						: displayName(path, false);
			};
		}
		if (path.equals("thaumonomicon_normal")) {
			return "Thaumonomicon";
		}
		if (path.equals("thaumometer")) {
			return "Thaumometer";
		}
		if (path.equals("goggles")) {
			return "Goggles of Revealing";
		}
		StringBuilder name = new StringBuilder();
		for (String part : path.split("_")) {
			if (!name.isEmpty()) {
				name.append(' ');
			}
			name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return name.toString();
	}

	private static final class ModBlockLootProvider extends FabricBlockLootSubProvider {
		private final CompletableFuture<HolderLookup.Provider> registryLookup;

		private ModBlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
			this.registryLookup = registryLookup;
		}

		@Override
		public void generate() {
			Holder<Enchantment> fortune = registryLookup.join()
					.lookupOrThrow(Registries.ENCHANTMENT)
					.getOrThrow(Enchantments.FORTUNE);
			dropSelf(DebugContent.TEST_RENDER_BLOCK);
			dropSelf(DebugContent.DEBUG_CONTAINER);
			for (Block block : ThaumcraftContent.BLOCKS) {
				if (block == ThaumcraftContent.ORE_AMBER) {
					LootItem.Builder amber = LootItem.lootTableItem(ThaumcraftContent.AMBER)
							.apply(SetItemCountFunction.setCount(Holder.direct(new UniformGenerator(
									Holder.direct(new ConstantValue(1)),
									Holder.direct(new ConstantValue(2))
							))))
							.apply(ApplyBonusCount.addOreBonusCount(fortune))
							.apply(ApplyExplosionDecay.explosionDecay());
					add(block, createSilkTouchDispatchTable(block, amber));
				} else if (block == ThaumcraftContent.ORE_CINNABAR) {
					dropSelf(block);
				} else if (block == ThaumcraftContent.ORE_QUARTZ) {
					add(block, createOreDrop(block, Items.QUARTZ));
				} else if (block == ThaumcraftContent.LEAVES_GREATWOOD) {
					add(block, createLeavesDrops(block, ThaumcraftContent.SAPLING_GREATWOOD, 1.0F / 75.0F));
				} else if (block == ThaumcraftContent.LEAVES_SILVERWOOD) {
					LootTable.Builder leaves = createLeavesDrops(block, ThaumcraftContent.SAPLING_SILVERWOOD, 1.0F / 75.0F);
					leaves.withPool(LootPool.lootPool()
							.when(LootItemRandomChanceCondition.randomChance(1.0F / 56.0F))
							.add(LootItem.lootTableItem(ThaumcraftContent.NUGGET_QUICKSILVER)));
					add(block, leaves);
				} else {
					dropSelf(block);
				}
			}
		}
	}
}
