package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.crystal.CrystalGrowthAccess;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class PrimalCrystalBlock extends Block {
	public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
	public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);

	private final Aspect aspect;

	public PrimalCrystalBlock(BlockBehaviour.Properties properties, Aspect aspect) {
		super(properties.randomTicks().noOcclusion().noCollision());
		this.aspect = aspect;
		this.registerDefaultState(this.defaultBlockState().setValue(SIZE, 0).setValue(GENERATION, 1));
	}

	public Aspect aspect() {
		return aspect;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos supportPos = pos.relative(direction);
			if (hasSupportedFace(level, supportPos, direction)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		boolean up = hasSupportedFace(level, pos.above(), Direction.UP);
		boolean down = hasSupportedFace(level, pos.below(), Direction.DOWN);
		boolean north = hasSupportedFace(level, pos.north(), Direction.NORTH);
		boolean east = hasSupportedFace(level, pos.east(), Direction.EAST);
		boolean south = hasSupportedFace(level, pos.south(), Direction.SOUTH);
		boolean west = hasSupportedFace(level, pos.west(), Direction.WEST);
		int supportCount = (up ? 1 : 0) + (down ? 1 : 0) + (north ? 1 : 0)
				+ (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);
		if (supportCount > 1) {
			return Shapes.block();
		}
		if (up) {
			return Shapes.box(0.0, 0.5, 0.0, 1.0, 1.0, 1.0);
		}
		if (down) {
			return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
		}
		if (east) {
			return Shapes.box(0.5, 0.0, 0.0, 1.0, 1.0, 1.0);
		}
		if (west) {
			return Shapes.box(0.0, 0.0, 0.0, 0.5, 1.0, 1.0);
		}
		if (south) {
			return Shapes.box(0.0, 0.0, 0.5, 1.0, 1.0, 1.0);
		}
		if (north) {
			return Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 0.5);
		}
		return Shapes.block();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SIZE, GENERATION);
	}

	@Override
	protected BlockState updateShape(
			BlockState state,
			LevelReader level,
			net.minecraft.world.level.ScheduledTickAccess scheduledTickAccess,
			BlockPos pos,
			net.minecraft.core.Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			RandomSource random
	) {
		if (!canSurvive(state, level, pos)) {
			if (level instanceof Level mutableLevel && !mutableLevel.isClientSide()) {
				Block.dropResources(state, mutableLevel, pos);
			}
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		CrystalGrowthAccess.get().tick(this, state, level, pos, random);
	}

	@Override
	protected java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		return java.util.stream.IntStream.range(0, state.getValue(SIZE) + 1)
				.mapToObj(ignored -> {
					ItemStack crystal = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
					crystal.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
					return crystal;
				})
				.toList();
	}

	private boolean hasSupportedFace(BlockGetter level, BlockPos supportPos, Direction direction) {
		BlockState support = level.getBlockState(supportPos);
		var collisionShape = support.getCollisionShape(level, supportPos);
		return Block.isFaceFull(collisionShape, direction.getOpposite())
				&& support.is(BlockTags.BASE_STONE_OVERWORLD);
	}
}
