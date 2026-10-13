package io.github.rdarkknight.thaumcraftreborn.content.research;

import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ScribingToolsItem extends Item {
	public ScribingToolsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		var level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(ThaumcraftContent.TABLE_WOOD) || level.isClientSide() || context.getPlayer() == null) {
			return InteractionResult.PASS;
		}
		ItemStack tools = context.getItemInHand().copyWithCount(1);
		if (!level.setBlock(pos, ThaumcraftContent.RESEARCH_TABLE.defaultBlockState(), Block.UPDATE_ALL)) {
			return InteractionResult.PASS;
		}
		if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
			table.setItem(0, tools);
			table.setChanged();
			if (!context.getPlayer().getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
			CraftEventRecorder.record((net.minecraft.server.level.ServerPlayer) context.getPlayer(),
					new ItemStack(ThaumcraftContent.RESEARCH_TABLE));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}
}
