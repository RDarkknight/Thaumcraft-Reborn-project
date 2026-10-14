package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import io.github.rdarkknight.thaumcraftreborn.api.research.ItemRequirement;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

	public static boolean hasCrafted(ServerPlayer player, ItemRequirement requirement) {
		Map<Identifier, Integer> crafted = player.getAttachedOrCreate(ModAttachments.CRAFTED_ITEMS);
		if (requirement.item().isPresent()) {
			return crafted.getOrDefault(requirement.item().orElseThrow().item(), 0) >= requirement.count();
		}
		TagKey<Item> tag = TagKey.create(Registries.ITEM, requirement.tag().orElseThrow());
		long count = crafted.entrySet().stream()
				.filter(entry -> new ItemStack(BuiltInRegistries.ITEM.getValue(entry.getKey())).is(tag))
				.mapToLong(Map.Entry::getValue)
				.sum();
		return count >= requirement.count();
	}
}
