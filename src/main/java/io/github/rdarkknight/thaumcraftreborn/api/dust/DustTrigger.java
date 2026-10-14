package io.github.rdarkknight.thaumcraftreborn.api.dust;

import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface DustTrigger {
	ResearchGate gate();

	boolean matches(Level level, BlockPos pos, BlockState state);

	ItemStack result(Level level, BlockPos pos, BlockState state);

	default List<Transformation> transformations(Level level, BlockPos pos, BlockState state) {
		return List.of(new Transformation(pos.immutable(), state, result(level, pos, state)));
	}

	record Transformation(BlockPos pos, BlockState original, ItemStack result) {
		public Transformation {
			pos = pos.immutable();
			result = result.copy();
		}
	}
}
