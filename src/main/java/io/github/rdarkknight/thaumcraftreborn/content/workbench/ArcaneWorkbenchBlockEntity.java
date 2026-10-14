package io.github.rdarkknight.thaumcraftreborn.content.workbench;

import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftContainerBlockEntity;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public final class ArcaneWorkbenchBlockEntity extends ThaumcraftContainerBlockEntity implements ExtendedMenuProvider<BlockPos> {
	public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
		super(ThaumcraftContent.ARCANE_WORKBENCH_BLOCK_ENTITY, pos, state, 15);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.thaumcraft_reborn.arcane_workbench");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new ArcaneWorkbenchMenu(containerId, inventory, getBlockPos());
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return getBlockPos();
	}
}
