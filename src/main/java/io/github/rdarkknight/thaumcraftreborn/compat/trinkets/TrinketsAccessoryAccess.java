package io.github.rdarkknight.thaumcraftreborn.compat.trinkets;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.rdarkknight.thaumcraftreborn.core.accessory.AccessoryAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class TrinketsAccessoryAccess implements AccessoryAccess {
	@Override
	public List<ItemStack> getEquipped(LivingEntity entity, Predicate<ItemStack> predicate) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		if (attachment == null) {
			return List.of();
		}
		List<ItemStack> equipped = new ArrayList<>();
		attachment.forEach((slot, stack) -> {
			if (!stack.isEmpty() && predicate.test(stack)) {
				equipped.add(stack);
			}
		});
		return List.copyOf(equipped);
	}

	@Override
	public void forEachEquipped(LivingEntity entity, Consumer<ItemStack> consumer) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		if (attachment == null) {
			return;
		}
		attachment.forEach((slot, stack) -> {
			if (!stack.isEmpty()) {
				consumer.accept(stack);
			}
		});
	}
}
