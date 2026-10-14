package io.github.rdarkknight.thaumcraftreborn.content.research;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.menu.ThaumcraftMenu;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.TheorycraftSystem;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.ResearchTableData;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ResearchTableMenu extends ThaumcraftMenu {
	public static final ExtendedMenuType<ResearchTableMenu, BlockPos> TYPE = Registry.register(
			BuiltInRegistries.MENU,
			ThaumcraftReborn.id("research_table"),
			new ExtendedMenuType<>(ResearchTableMenu::new, BlockPos.STREAM_CODEC)
	);
	public static final int START_SESSION = 0;
	public static final int DRAW_TWO = 1;
	public static final int DRAW_THREE = 2;
	public static final int PICK_CARD_BASE = 10;
	private final BlockPos pos;

	public static void init() {
	}

	public ResearchTableMenu(int containerId, Inventory inventory, BlockPos pos) {
		super(TYPE, containerId, createMachine(inventory, pos));
		this.pos = pos;
		addSlot(new Slot(machine(), 0, 26, 24) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ThaumcraftContent.SCRIBING_TOOLS);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		addSlot(new Slot(machine(), 1, 62, 24) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(net.minecraft.world.item.Items.PAPER);
			}
		});
		addPlayerInventory(inventory, 8, 78);
	}

	private static Container createMachine(Inventory inventory, BlockPos pos) {
		if (inventory.player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity blockEntity) {
			return blockEntity;
		}
		return new SimpleContainer(2);
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()
				|| !(player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity table)
				|| !stillValid(player)) {
			return false;
		}
		boolean changed = switch (buttonId) {
			case START_SESSION -> TheorycraftSystem.start(table, serverPlayer);
			case DRAW_TWO -> TheorycraftSystem.draw(table, serverPlayer, 2);
			case DRAW_THREE -> TheorycraftSystem.draw(table, serverPlayer, 3);
			default -> buttonId >= PICK_CARD_BASE
					&& TheorycraftSystem.pick(table, serverPlayer, buttonId - PICK_CARD_BASE);
		};
		return changed;
	}

	public ResearchTableData data() {
		return machine() instanceof ResearchTableBlockEntity table ? table.data() : new ResearchTableData();
	}
}
