package io.github.rdarkknight.thaumcraftreborn.systems;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class PickupResearchFlags {
	private static final net.minecraft.resources.Identifier GOT_THAUMONOMICON =
			ThaumcraftRebornApi.id("flag/got_thaumonomicon");
	private static final net.minecraft.resources.Identifier GOT_CRYSTALS =
			ThaumcraftRebornApi.id("flag/got_crystals");
	private static final net.minecraft.resources.Identifier GOT_DREAM =
			ThaumcraftRebornApi.id("flag/got_dream");

	private PickupResearchFlags() {
	}

	public static void init() {
		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (entity instanceof ServerPlayer player) {
				onWakeup(player);
			}
		});
	}

	public static void onPickup(ServerPlayer player, ItemStack stack) {
		if (stack.is(ThaumcraftContent.THAUMONOMICON)) {
			KnowledgeAccess.get().addResearch(player, GOT_THAUMONOMICON);
		}
		if (stack.is(ThaumcraftContent.CRYSTAL_ESSENCE)) {
			KnowledgeAccess.get().addResearch(player, GOT_CRYSTALS);
		}
	}

	public static void onPickupAttempt(ServerPlayer player, ItemEntity itemEntity, int pickupDelay, UUID target) {
		if (itemEntity.level().isClientSide()
				|| pickupDelay != 0
				|| (target != null && !target.equals(player.getUUID()))) {
			return;
		}
		onPickup(player, itemEntity.getItem());
	}

	public static void onWakeup(ServerPlayer player) {
		var knowledge = KnowledgeAccess.get().knowledge(player);
		if (knowledge.isResearchKnown(GOT_CRYSTALS) && !knowledge.isResearchKnown(GOT_DREAM)) {
			KnowledgeAccess.get().addResearch(player, GOT_DREAM);
		}
	}

}
