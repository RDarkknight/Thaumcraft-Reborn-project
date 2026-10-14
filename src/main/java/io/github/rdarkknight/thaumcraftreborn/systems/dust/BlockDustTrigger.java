package io.github.rdarkknight.thaumcraftreborn.systems.dust;

import io.github.rdarkknight.thaumcraftreborn.api.dust.DustTrigger;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

record BlockDustTrigger(
		ResearchGate gate,
		Predicate<BlockState> matcher,
		Supplier<ItemStack> resultFactory
) implements DustTrigger {
	@Override
	public boolean matches(Level level, BlockPos pos, BlockState state) {
		return matcher.test(state);
	}

	@Override
	public ItemStack result(Level level, BlockPos pos, BlockState state) {
		return resultFactory.get().copy();
	}
}
