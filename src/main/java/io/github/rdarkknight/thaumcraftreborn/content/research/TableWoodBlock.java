package io.github.rdarkknight.thaumcraftreborn.content.research;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public final class TableWoodBlock extends Block {
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(0, 12, 0, 16, 16, 16),
			Block.box(1, 0, 1, 3, 12, 3),
			Block.box(13, 0, 1, 15, 12, 3),
			Block.box(1, 0, 13, 3, 12, 15),
			Block.box(13, 0, 13, 15, 12, 15)
	);

	public TableWoodBlock(BlockBehaviour.Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	public VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state,
			net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(net.minecraft.world.level.block.state.BlockState state,
			net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
