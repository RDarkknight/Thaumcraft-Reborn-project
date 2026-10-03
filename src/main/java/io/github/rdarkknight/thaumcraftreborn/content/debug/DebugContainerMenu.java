package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.core.menu.ThaumcraftMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class DebugContainerMenu extends ThaumcraftMenu {
	public static final ExtendedMenuType<DebugContainerMenu, BlockPos> TYPE = Registry.register(
			BuiltInRegistries.MENU,
			ThaumcraftReborn.id("debug_container"),
			new ExtendedMenuType<>(DebugContainerMenu::new, BlockPos.STREAM_CODEC)
	);

	private final ContainerData data;

	public DebugContainerMenu(int containerId, Inventory inventory, BlockPos pos) {
		super(TYPE, containerId, pos, 9);
		BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
		Container container = blockEntity instanceof DebugContainerBlockEntity debugContainer
				? debugContainer
				: new SimpleContainer(9);
		for (int slot = 0; slot < 9; slot++) {
			addSlot(new net.minecraft.world.inventory.Slot(container, slot, 62 + slot % 3 * 18, 17 + slot / 3 * 18));
		}
		addPlayerInventory(inventory, 8, 84);

		this.data = new ContainerData() {
			private int synchronizedTicks;

			@Override
			public int get(int index) {
				if (index != 0) {
					return 0;
				}
				if (!inventory.player.level().isClientSide() && blockEntity instanceof DebugContainerBlockEntity debugContainer) {
					return debugContainer.ticks();
				}
				return synchronizedTicks;
			}

			@Override
			public void set(int index, int value) {
				if (index == 0) {
					synchronizedTicks = value;
				}
			}

			@Override
			public int getCount() {
				return 1;
			}
		};
		addDataSlots(data);
	}

	public static void init() {
	}

	public int ticks() {
		return data.get(0);
	}
}
