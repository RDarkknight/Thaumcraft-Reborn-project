package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class CraftEventRecorder {
	private CraftEventRecorder() {
	}

	public static void record(ServerPlayer player, ItemStack result) {
		if (result.isEmpty()) {
			return;
		}
		Identifier item = BuiltInRegistries.ITEM.getKey(result.getItem());
		Map<Identifier, Integer> updated = new HashMap<>(player.getAttachedOrCreate(ModAttachments.CRAFTED_ITEMS));
		updated.merge(item, result.getCount(), (previous, added) -> (int) Math.min(Integer.MAX_VALUE, (long) previous + added));
		player.setAttached(ModAttachments.CRAFTED_ITEMS, Map.copyOf(updated));
	}

	public static int count(ServerPlayer player, Identifier item) {
		return player.getAttachedOrCreate(ModAttachments.CRAFTED_ITEMS).getOrDefault(item, 0);
	}
}
