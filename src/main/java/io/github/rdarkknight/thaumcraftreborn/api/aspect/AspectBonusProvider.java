package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface AspectBonusProvider {
	AspectList apply(Level level, ItemStack stack, AspectList aspects);
}
