package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class DebugContainerBlock extends ThaumcraftEntityBlock {
	public DebugContainerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DebugContainerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return serverTicker(level, type, DebugContent.DEBUG_CONTAINER_BLOCK_ENTITY, DebugContainerBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DebugContainerBlockEntity blockEntity) {
			player.openMenu(blockEntity);
		}
		return InteractionResult.SUCCESS;
	}
}
