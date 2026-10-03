package io.github.rdarkknight.thaumcraftreborn.core.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class ThaumcraftEntityBlock extends BaseEntityBlock {
	protected ThaumcraftEntityBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> serverTicker(
			Level level,
			BlockEntityType<A> actual,
			BlockEntityType<E> expected,
			BlockEntityTicker<? super E> ticker
	) {
		return level.isClientSide() ? null : createTickerHelper(actual, expected, ticker);
	}
}
