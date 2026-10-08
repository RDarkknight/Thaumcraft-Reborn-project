package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface AspectLookup {
	AspectLookup NONE = new AspectLookup() {
		@Override
		public AspectList getAspects(Level level, ItemStack stack) {
			return AspectList.EMPTY;
		}

		@Override
		public AspectList getBaseAspects(Level level, ItemStack stack) {
			return AspectList.EMPTY;
		}

		@Override
		public AspectList getAspects(Entity entity) {
			return AspectList.EMPTY;
		}
	};

	static AspectLookup get() {
		return Holder.INSTANCE;
	}

	static void install(AspectLookup lookup) {
		Holder.INSTANCE = Objects.requireNonNull(lookup);
	}

	AspectList getAspects(Level level, ItemStack stack);

	AspectList getBaseAspects(Level level, ItemStack stack);

	AspectList getAspects(Entity entity);

	final class Holder {
		private static volatile AspectLookup INSTANCE = NONE;

		private Holder() {
		}
	}
}
