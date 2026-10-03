package io.github.rdarkknight.thaumcraftreborn.content.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TestRenderBlockEntity extends BlockEntity {
	public TestRenderBlockEntity(BlockPos pos, BlockState state) {
		super(DebugContent.TEST_RENDER_BLOCK_ENTITY, pos, state);
	}
}
