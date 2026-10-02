package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;

public final class DebugContent {
	public static final Item TEST_PROBE = Registry.register(
			BuiltInRegistries.ITEM,
			ThaumcraftReborn.id("test_probe"),
			new ProbeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id("test_probe"))))
	);
	public static final Block TEST_RENDER_BLOCK = Registry.register(
			BuiltInRegistries.BLOCK,
			ThaumcraftReborn.id("test_render_block"),
			new TestRenderBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ThaumcraftReborn.id("test_render_block"))))
	);
	public static final Item TEST_RENDER_BLOCK_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			ThaumcraftReborn.id("test_render_block"),
			new BlockItem(TEST_RENDER_BLOCK, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ThaumcraftReborn.id("test_render_block"))))
	);
	public static final BlockEntityType<TestRenderBlockEntity> TEST_RENDER_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ThaumcraftReborn.id("test_render_block"),
			new BlockEntityType<>(TestRenderBlockEntity::new, Set.of(TEST_RENDER_BLOCK))
	);

	private DebugContent() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(TEST_PROBE));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> output.accept(TEST_RENDER_BLOCK_ITEM));
	}
}
