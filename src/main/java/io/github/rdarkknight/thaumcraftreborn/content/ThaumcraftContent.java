package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.item.ItemStack;

public final class ThaumcraftContent {
	public static final Item AMBER = registerItem("amber", new Item.Properties());
	public static final Item QUICKSILVER = registerItem("quicksilver", new Item.Properties());
	public static final Item INGOT_THAUMIUM = registerItem("ingot_thaumium", new Item.Properties());
	public static final Item NUGGET_THAUMIUM = registerItem("nugget_thaumium", new Item.Properties());
	public static final Item NUGGET_QUARTZ = registerItem("nugget_quartz", new Item.Properties());
	public static final Item NUGGET_QUICKSILVER = registerItem("nugget_quicksilver", new Item.Properties());
	public static final Item CRYSTAL_ESSENCE = registerItem("crystal_essence", new Item.Properties());
	public static final Item THAUMOMETER = registerCustomItem("thaumometer", ThaumometerItem::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	private static final net.minecraft.tags.TagKey<Item> GOGGLES_REPAIR_ITEMS =
			net.minecraft.tags.TagKey.create(Registries.ITEM, ThaumcraftReborn.id("repairs/goggles"));
	private static final ArmorMaterial GOGGLES_ARMOR_MATERIAL = new ArmorMaterial(
			25,
			java.util.Map.of(
					ArmorType.HELMET, 1,
					ArmorType.CHESTPLATE, 3,
					ArmorType.LEGGINGS, 2,
					ArmorType.BOOTS, 1,
					ArmorType.BODY, 0
			),
			25,
			SoundEvents.ARMOR_EQUIP_LEATHER,
			1.0F,
			0.0F,
			GOGGLES_REPAIR_ITEMS,
			ArmorMaterials.LEATHER.assetId()
	);
	public static final Item GOGGLES = registerItem("goggles",
			new Item.Properties().humanoidArmor(GOGGLES_ARMOR_MATERIAL, ArmorType.HELMET)
					.durability(350).rarity(Rarity.RARE).repairable(GOGGLES_REPAIR_ITEMS));
	public static final Item THAUMONOMICON = registerCustomItem("thaumonomicon_normal", ThaumonomiconItem::new,
			new Item.Properties().stacksTo(1));

	public static final Block ORE_AMBER = registerBlock("ore_amber",
			properties -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(1, 4), properties),
			1.5F, 5.0F, true);
	public static final Block ORE_CINNABAR = registerBlock("ore_cinnabar", properties -> new Block(properties), 2.0F, 5.0F, true);
	public static final Block ORE_QUARTZ = registerBlock("ore_quartz",
			properties -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(1, 4), properties),
			3.0F, 5.0F, false);

	public static final Block CRYSTAL_AER = registerCrystal("crystal_aer", Aspects.AIR);
	public static final Block CRYSTAL_TERRA = registerCrystal("crystal_terra", Aspects.EARTH);
	public static final Block CRYSTAL_IGNIS = registerCrystal("crystal_ignis", Aspects.FIRE);
	public static final Block CRYSTAL_AQUA = registerCrystal("crystal_aqua", Aspects.WATER);
	public static final Block CRYSTAL_ORDO = registerCrystal("crystal_ordo", Aspects.ORDER);
	public static final Block CRYSTAL_PERDITIO = registerCrystal("crystal_perditio", Aspects.ENTROPY);

	public static final Block STONE_ARCANE = registerBlock("stone_arcane", Block::new, 2.0F, 10.0F, false);
	public static final Block STONE_ARCANE_BRICK = registerBlock("stone_arcane_brick", Block::new, 2.0F, 10.0F, false);
	public static final Block AMBER_BLOCK = registerBlock("amber_block", Block::new, 0.5F, 2.0F, false);
	public static final Block AMBER_BRICK = registerBlock("amber_brick", Block::new, 0.5F, 2.0F, false);
	public static final Block METAL_THAUMIUM = registerBlock("metal_thaumium", Block::new, 4.0F, 10.0F, false);

	public static final Block LOG_GREATWOOD = registerBlock("log_greatwood",
			properties -> new RotatedPillarBlock(properties), 2.0F, 5.0F, false);
	public static final Block LOG_SILVERWOOD = registerBlock("log_silverwood",
			properties -> new RotatedPillarBlock(properties.lightLevel(state -> 5)), 2.0F, 5.0F, false);
	public static final Block PLANK_GREATWOOD = registerBlock("plank_greatwood", Block::new, 2.0F, 2.0F, false);
	public static final Block PLANK_SILVERWOOD = registerBlock("plank_silverwood", Block::new, 2.0F, 2.0F, false);
	public static final Block LEAVES_GREATWOOD = registerBlock("leaves_greatwood",
			properties -> new LeavesBlock(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties), 0.2F, 0.2F, false);
	public static final Block LEAVES_SILVERWOOD = registerBlock("leaves_silverwood",
			properties -> new LeavesBlock(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties), 0.2F, 0.2F, false);
	public static final Block SAPLING_GREATWOOD = registerBlock("sapling_greatwood", SaplingPlaceholderBlock::new, 0.0F, 0.0F, false);
	public static final Block SAPLING_SILVERWOOD = registerBlock("sapling_silverwood", SaplingPlaceholderBlock::new, 0.0F, 0.0F, false);

	private static final net.minecraft.tags.TagKey<Item> THAUMIUM_REPAIR_ITEMS =
			net.minecraft.tags.TagKey.create(Registries.ITEM, ThaumcraftReborn.id("repairs/thaumium_tools"));
	public static final ToolMaterial THAUMIUM_TOOL_MATERIAL =
			new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22, THAUMIUM_REPAIR_ITEMS);

	public static final Item THAUMIUM_SWORD = registerTool("thaumium_sword",
			new Item.Properties().sword(THAUMIUM_TOOL_MATERIAL, 3.0F, -2.4F));
	public static final Item THAUMIUM_AXE = registerTool("thaumium_axe",
			new Item.Properties().axe(THAUMIUM_TOOL_MATERIAL, 8.0F, -3.0F));
	public static final Item THAUMIUM_PICK = registerTool("thaumium_pick",
			new Item.Properties().pickaxe(THAUMIUM_TOOL_MATERIAL, 1.0F, -2.8F));
	public static final Item THAUMIUM_SHOVEL = registerTool("thaumium_shovel",
			new Item.Properties().shovel(THAUMIUM_TOOL_MATERIAL, 1.5F, -3.0F));
	public static final Item THAUMIUM_HOE = registerTool("thaumium_hoe",
			new Item.Properties().hoe(THAUMIUM_TOOL_MATERIAL, 0.0F, -3.0F));

	public static final List<Block> BLOCKS = List.of(
			ORE_AMBER, ORE_CINNABAR, ORE_QUARTZ, CRYSTAL_AER, CRYSTAL_TERRA, CRYSTAL_IGNIS, CRYSTAL_AQUA,
			CRYSTAL_ORDO, CRYSTAL_PERDITIO, STONE_ARCANE, STONE_ARCANE_BRICK, AMBER_BLOCK, AMBER_BRICK,
			METAL_THAUMIUM, LOG_GREATWOOD, LOG_SILVERWOOD, PLANK_GREATWOOD, PLANK_SILVERWOOD,
			LEAVES_GREATWOOD, LEAVES_SILVERWOOD, SAPLING_GREATWOOD, SAPLING_SILVERWOOD
	);
	public static final List<Item> ITEMS = List.of(
			AMBER, QUICKSILVER, INGOT_THAUMIUM, NUGGET_THAUMIUM, NUGGET_QUARTZ, NUGGET_QUICKSILVER,
			THAUMIUM_SWORD, THAUMIUM_AXE, THAUMIUM_PICK, THAUMIUM_SHOVEL, THAUMIUM_HOE, CRYSTAL_ESSENCE,
			THAUMOMETER, GOGGLES, THAUMONOMICON
	);
	private static final List<ResourceKey<Aspect>> PRIMAL_ASPECTS =
			List.of(Aspects.AIR, Aspects.EARTH, Aspects.FIRE, Aspects.WATER, Aspects.ORDER, Aspects.ENTROPY);

	private ThaumcraftContent() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				.register(output -> {
					ITEMS.stream().filter(item -> item != CRYSTAL_ESSENCE).forEach(output::accept);
					for (ResourceKey<Aspect> aspectKey : PRIMAL_ASPECTS) {
						Aspect aspect = Objects.requireNonNull(ThaumcraftRegistries.ASPECT.getValue(aspectKey.identifier()));
						ItemStack essence = new ItemStack(CRYSTAL_ESSENCE);
						essence.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
						output.accept(essence);
					}
				});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
				.register(output -> BLOCKS.forEach(block -> output.accept(block.asItem())));
	}

	private static Block registerCrystal(String name, ResourceKey<Aspect> aspectKey) {
		Aspect aspect = Objects.requireNonNull(ThaumcraftRegistries.ASPECT.getValue(aspectKey.identifier()));
		return registerBlock(name, properties -> new PrimalCrystalBlock(properties, aspect), 0.25F, 0.0F, false);
	}

	private static Block registerBlock(
			String name,
			Function<BlockBehaviour.Properties, Block> factory,
			float hardness,
			float resistance,
			boolean requiresCorrectTool
	) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ThaumcraftReborn.id(name));
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.strength(hardness, resistance)
				.setId(blockKey);
		if (requiresCorrectTool) {
			properties.requiresCorrectToolForDrops();
		}
		Block block = Registry.register(BuiltInRegistries.BLOCK, ThaumcraftReborn.id(name), factory.apply(properties));
		Item item = new BlockItem(block, new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id(name))));
		Registry.register(BuiltInRegistries.ITEM, ThaumcraftReborn.id(name), item);
		return block;
	}

	private static Item registerItem(String name, Item.Properties properties) {
		return Registry.register(BuiltInRegistries.ITEM, ThaumcraftReborn.id(name),
				new Item(properties.setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id(name)))));
	}

	private static Item registerCustomItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		return Registry.register(BuiltInRegistries.ITEM, ThaumcraftReborn.id(name),
				factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id(name)))));
	}

	private static Item registerTool(String name, Item.Properties properties) {
		return Registry.register(BuiltInRegistries.ITEM, ThaumcraftReborn.id(name),
				new Item(properties.setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id(name)))));
	}

	private static final class SaplingPlaceholderBlock extends BushBlock {
		private static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

		private SaplingPlaceholderBlock(BlockBehaviour.Properties properties) {
			super(properties.noOcclusion().noCollision().strength(0.0F));
			this.registerDefaultState(this.defaultBlockState().setValue(STAGE, 0));
		}

		@Override
		public boolean isValidBonemealTarget(
				net.minecraft.world.level.LevelReader level,
				net.minecraft.core.BlockPos pos,
				net.minecraft.world.level.block.state.BlockState state,
				net.minecraft.world.level.block.BonemealSource bonemealSource
		) {
			return false;
		}

		@Override
		public boolean isBonemealSuccess(
				net.minecraft.world.level.Level level,
				net.minecraft.util.RandomSource random,
				net.minecraft.core.BlockPos pos,
				net.minecraft.world.level.block.state.BlockState state,
				net.minecraft.world.level.block.BonemealSource bonemealSource
		) {
			return false;
		}

		@Override
		public void performBonemeal(
				net.minecraft.server.level.ServerLevel level,
				net.minecraft.util.RandomSource random,
				net.minecraft.core.BlockPos pos,
				net.minecraft.world.level.block.state.BlockState state,
				net.minecraft.world.level.block.BonemealSource bonemealSource
		) {
		}

		@Override
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, net.minecraft.world.level.block.state.BlockState> builder) {
			builder.add(STAGE);
		}
	}
}
