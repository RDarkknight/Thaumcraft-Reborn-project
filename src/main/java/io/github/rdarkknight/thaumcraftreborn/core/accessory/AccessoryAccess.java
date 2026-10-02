package io.github.rdarkknight.thaumcraftreborn.core.accessory;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface AccessoryAccess {
	AccessoryAccess NONE = new AccessoryAccess() {
		@Override
		public List<ItemStack> getEquipped(LivingEntity entity, Predicate<ItemStack> predicate) {
			return List.of();
		}

		@Override
		public void forEachEquipped(LivingEntity entity, Consumer<ItemStack> consumer) {
		}
	};

	static AccessoryAccess get() {
		return Holder.INSTANCE;
	}

	static void install(AccessoryAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	List<ItemStack> getEquipped(LivingEntity entity, Predicate<ItemStack> predicate);

	default boolean isEquipped(LivingEntity entity, Item item) {
		return !getEquipped(entity, stack -> stack.is(item)).isEmpty();
	}

	void forEachEquipped(LivingEntity entity, Consumer<ItemStack> consumer);

	final class Holder {
		private static volatile AccessoryAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
