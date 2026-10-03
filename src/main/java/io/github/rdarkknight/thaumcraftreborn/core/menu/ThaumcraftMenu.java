package io.github.rdarkknight.thaumcraftreborn.core.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ThaumcraftMenu extends AbstractContainerMenu {
	private final Container machine;
	private final int machineSlotCount;

	protected ThaumcraftMenu(MenuType<?> type, int containerId, Container machine) {
		super(type, containerId);
		this.machine = machine;
		this.machineSlotCount = machine.getContainerSize();
	}

	protected Container machine() {
		return machine;
	}

	protected void addPlayerInventory(Inventory inventory, int x, int y) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(inventory, column, x + column * 18, y + 58));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index < 0 || index >= slots.size()) {
			return ItemStack.EMPTY;
		}

		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		boolean moved = index < machineSlotCount
				? moveItemStackTo(stack, machineSlotCount, slots.size(), true)
				: moveItemStackTo(stack, 0, machineSlotCount, false);
		if (!moved) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (stack.getCount() == original.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, stack);
		return original;
	}

	@Override
	public boolean stillValid(Player player) {
		return machine.stillValid(player);
	}
}
