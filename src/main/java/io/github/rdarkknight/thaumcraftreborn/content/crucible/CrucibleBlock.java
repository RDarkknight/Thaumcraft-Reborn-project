package io.github.rdarkknight.thaumcraftreborn.content.crucible;

import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CrucibleBlock extends ThaumcraftEntityBlock {
	public static final TagKey<Block> HEATERS = TagKey.create(Registries.BLOCK,
			io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn.id("crucible_heaters"));
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(0, 0, 0, 16, 5, 16),
			Block.box(0, 0, 0, 2, 16, 16),
			Block.box(14, 0, 0, 16, 16, 16),
			Block.box(2, 0, 0, 14, 16, 2),
			Block.box(2, 0, 14, 14, 16, 16)
	);

	public CrucibleBlock(BlockBehaviour.Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrucibleBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return serverTicker(level, type, ThaumcraftContent.CRUCIBLE_BLOCK_ENTITY, CrucibleBlockEntity::serverTick);
	}

	@Override
	protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
			net.minecraft.world.phys.shapes.CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			net.minecraft.world.entity.InsideBlockEffectApplier effects, boolean intersects) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
			crucible.entityInside(entity);
		}
		super.entityInside(state, level, pos, entity, effects, intersects);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
			if (!level.isClientSide()) {
				crucible.spillRemnants();
			}
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
			return InteractionResult.PASS;
		}
		if (stack.is(Items.WATER_BUCKET) && crucible.waterMb() < CrucibleBlockEntity.CAPACITY) {
			int amount = Math.min(1000, CrucibleBlockEntity.CAPACITY - crucible.waterMb());
			crucible.setWaterMb(crucible.waterMb() + amount);
			if (!player.getAbilities().instabuild) {
				if (stack.getCount() == 1) {
					player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				} else {
					stack.shrink(1);
					player.getInventory().placeItemBackInInventory(new ItemStack(Items.BUCKET), net.minecraft.util.Prediction.SERVER_ONLY);
				}
			}
			return InteractionResult.SUCCESS;
		}
		if (!player.isShiftKeyDown() && hit.getDirection() == net.minecraft.core.Direction.UP && !stack.isEmpty()) {
			ItemStack remaining = crucible.processItem(stack.copyWithCount(1),
					player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
			if (remaining.isEmpty()) {
				stack.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}
