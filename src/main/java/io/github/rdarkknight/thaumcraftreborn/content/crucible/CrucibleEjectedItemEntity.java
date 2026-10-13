package io.github.rdarkknight.thaumcraftreborn.content.crucible;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class CrucibleEjectedItemEntity extends ItemEntity {
	CrucibleEjectedItemEntity(Level level, double x, double y, double z, ItemStack item) {
		super(level, x, y, z, item);
	}
}
