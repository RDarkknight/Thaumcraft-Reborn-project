package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftContainerBlockEntity;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class DebugContainerBlockEntity extends ThaumcraftContainerBlockEntity implements ExtendedMenuProvider<BlockPos> {
	private int ticks;

	public DebugContainerBlockEntity(BlockPos pos, BlockState state) {
		super(DebugContent.DEBUG_CONTAINER_BLOCK_ENTITY, pos, state, 9);
	}

	public int ticks() {
		return ticks;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DebugContainerBlockEntity blockEntity) {
		if (!level.isClientSide()) {
			blockEntity.ticks++;
			if (blockEntity.ticks % 20 == 0) {
				blockEntity.markDirtyAndSync();
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ticks = input.read("ticks", Codec.INT).orElse(0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("ticks", Codec.INT, ticks);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.thaumcraft_reborn.debug_container");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new DebugContainerMenu(containerId, inventory, getBlockPos());
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return getBlockPos();
	}
}
