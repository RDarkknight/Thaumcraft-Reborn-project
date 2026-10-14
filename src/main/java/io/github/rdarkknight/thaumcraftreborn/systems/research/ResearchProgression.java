package io.github.rdarkknight.thaumcraftreborn.systems.research;

import io.github.rdarkknight.thaumcraftreborn.api.item.ItemReference;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerKnowledge;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeAmount;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ItemRequirement;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAddendum;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchEntry;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchMeta;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchReference;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchRequirement;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStage;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ResearchProgression {
	private ResearchProgression() {
	}

	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> unlockAutomaticResearch(handler.getPlayer()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				server.getPlayerList().getPlayers().forEach(ResearchProgression::unlockAutomaticResearch);
			}
		});
	}

	public static void unlockAutomaticResearch(ServerPlayer player) {
		ResearchAccess.get().entries(false).forEach((key, entry) -> {
			if (entry.meta().contains(ResearchMeta.AUTOUNLOCK)) {
				KnowledgeAccess.get().addResearch(player, key);
			}
		});
	}

	public static boolean progressResearch(ServerPlayer player, Identifier key, boolean sync) {
		Set<Identifier> path = new HashSet<>();
		path.add(key);
		return progressOne(player, key, sync, path);
	}

	public static boolean progressStage(ServerPlayer player, Identifier key) {
		ResearchEntry entry = ResearchAccess.get().entry(key, false).orElse(null);
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (entry == null || !knowledge.isResearchKnown(key) || !doesPlayerHaveRequisites(player, key)) {
			return false;
		}
		int stageIndex = knowledge.getResearchStage(key) - 1;
		if (stageIndex < 0 || stageIndex >= entry.stages().size()) {
			return false;
		}
		ResearchStage stage = entry.stages().get(stageIndex);
		int[] itemCosts = itemCosts(player.getInventory(), stage.requiredItem());
		if (itemCosts == null || !hasStageRequirements(player, knowledge, stage)) {
			return false;
		}
		Map<KnowledgeKey, Long> knowledgeCosts = knowledgeCosts(knowledge, stage.requiredKnowledge());
		if (knowledgeCosts == null) {
			return false;
		}
		List<KnowledgeCost> deducted = new ArrayList<>();
		for (Map.Entry<KnowledgeKey, Long> cost : knowledgeCosts.entrySet()) {
			int amount = Math.toIntExact(cost.getValue());
			KnowledgeKey keyCost = cost.getKey();
			if (!KnowledgeAccess.get().addKnowledge(player, keyCost.type(), keyCost.category(), -amount)) {
				for (KnowledgeCost previous : deducted) {
					KnowledgeAccess.get().addKnowledge(player, previous.key().type(), previous.key().category(), previous.amount());
				}
				return false;
			}
			deducted.add(new KnowledgeCost(keyCost, amount));
		}
		consumeItems(player.getInventory(), itemCosts);
		return progressResearch(player, key, true);
	}

	public static boolean handleProgressRequest(ServerPlayer player, Identifier key) {
		ResearchEntry entry = ResearchAccess.get().entry(key, false).orElse(null);
		if (entry == null) {
			return false;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (!knowledge.isResearchKnown(key)) {
			if (!doesPlayerHaveRequisites(player, key)
					|| entry.meta().contains(ResearchMeta.HIDDEN)
							&& !isVisible(player, key, new HashSet<>())) {
				return false;
			}
			return progressResearch(player, key, true);
		}
		return knowledge.getResearchStatus(key) != ResearchStatus.COMPLETE && progressStage(player, key);
	}

	public static void completeKnownSiblings(ServerPlayer player) {
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		ResearchAccess.get().entries(false).forEach((key, entry) -> {
			if (knowledge.isResearchKnown(key)) {
				entry.siblings().forEach(sibling -> {
					if (!knowledge.isResearchKnown(sibling.key())) {
						completeResearch(player, sibling.key(), true);
					}
				});
			}
		});
	}

	public static boolean isVisible(Player player, Identifier key, Set<Identifier> invisibleCache) {
		return isVisible(player, key, invisibleCache, new HashSet<>());
	}

	private static boolean isVisible(
			Player player,
			Identifier key,
			Set<Identifier> invisibleCache,
			Set<Identifier> resolving
	) {
		ResearchEntry entry = ResearchAccess.get().entry(key, player.level().isClientSide()).orElse(null);
		if (entry == null) {
			return false;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (knowledge.isResearchKnown(key)) {
			return true;
		}
		if (invisibleCache.contains(key)) {
			return false;
		}
		boolean hidden = entry.meta().contains(ResearchMeta.HIDDEN);
		if (hidden && (!doesPlayerHaveRequisites(player, key) || entry.parents().isEmpty())) {
			invisibleCache.add(key);
			return false;
		}
		if (!resolving.add(key)) {
			invisibleCache.add(key);
			return false;
		}
		try {
			for (ResearchReference parent : entry.parents()) {
				if (ResearchAccess.get().entry(parent.key(), player.level().isClientSide()).isPresent()
						&& !isVisible(player, parent.key(), invisibleCache, resolving)) {
					invisibleCache.add(parent.key());
					return false;
				}
			}
		} finally {
			resolving.remove(key);
		}
		return true;
	}

	public static boolean completeResearch(ServerPlayer player, Identifier key, boolean sync) {
		return completeResearch(player, key, sync, new HashSet<>());
	}

	public static boolean giveRecursiveResearch(ServerPlayer player, Identifier key) {
		return giveRecursiveResearch(player, key, new HashSet<>());
	}

	public static boolean revokeRecursiveResearch(ServerPlayer player, Identifier key) {
		return revokeRecursiveResearch(player, key, new HashSet<>());
	}

	public static boolean doesPlayerHaveRequisites(Player player, Identifier key) {
		Optional<ResearchEntry> entry = ResearchAccess.get().entry(key, player.level().isClientSide());
		if (entry.isEmpty()) {
			return true;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		return entry.get().parents().stream().allMatch(parent -> parent.stage().isPresent()
				? knowledge.isResearchKnown(parent)
				: knowledge.isResearchComplete(parent.key()));
	}

	public static boolean isUnlocked(Player player, Identifier key, OptionalInt stage) {
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (stage.isPresent()) {
			return knowledge.isResearchKnown(new ResearchReference(key, stage, false));
		}
		return knowledge.isResearchComplete(key);
	}

	private static boolean completeResearch(
			ServerPlayer player,
			Identifier key,
			boolean sync,
			Set<Identifier> recursionPath
	) {
		if (!recursionPath.add(key)) {
			return false;
		}
		boolean progressed = false;
		try {
			while (progressOne(player, key, sync, recursionPath)) {
				progressed = true;
			}
		} finally {
			recursionPath.remove(key);
		}
		return progressed;
	}

	private static boolean progressOne(ServerPlayer player, Identifier key, boolean sync, Set<Identifier> recursionPath) {
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (knowledge.getResearchStatus(key) == ResearchStatus.COMPLETE || !doesPlayerHaveRequisites(player, key)) {
			return false;
		}
		if (!knowledge.isResearchKnown(key)) {
			KnowledgeAccess.get().addResearch(player, key);
		}
		ResearchEntry entry = ResearchAccess.get().entry(key, false).orElse(null);
		boolean popups = true;
		if (entry != null) {
			List<ResearchStage> stages = entry.stages();
			int cs = knowledge.getResearchStage(key);
			ResearchStage currentStage = null;
			if (cs > 0) {
				cs = Math.min(cs, stages.size());
				currentStage = stages.get(cs - 1);
			}
			if (stages.size() == 1 && cs == 0 && hasNoRequirements(stages.getFirst())) {
				cs++;
			} else if (stages.size() > 1 && stages.size() <= cs + 1 && cs < stages.size()
					&& hasNoRequirements(stages.get(cs))) {
				cs++;
			}
			KnowledgeAccess.get().setResearchStage(player, key, Math.min(stages.size() + 1, cs + 1));
			popups = cs >= stages.size();
			int warp = currentStage == null ? 0 : currentStage.warp();
			if (popups) {
				cs = Math.min(cs, stages.size());
				currentStage = stages.get(cs - 1);
			}
			if (currentStage != null) {
				warp += currentStage.warp();
				if (warp > 0 && !ThaumcraftConfig.common(false).misc().wussMode()) {
					addResearchWarp(player, warp);
				}
			}
			if (popups) {
				if (sync) {
					KnowledgeAccess.get().setResearchFlag(player, key, ResearchFlag.POPUP);
					KnowledgeAccess.get().setResearchFlag(player, key, ResearchFlag.RESEARCH);
					giveRewards(player, entry);
				}
				notifyAddenda(player, key);
			}
		}

		for (ResearchReference sibling : entry == null ? List.<ResearchReference>of() : entry.siblings()) {
			if (!knowledge.isResearchComplete(sibling.key())
					&& doesPlayerHaveRequisites(player, sibling.key())) {
				completeResearch(player, sibling.key(), sync, recursionPath);
			}
		}
		if (sync && entry != null) {
			player.giveExperiencePoints(5);
		}
		return true;
	}

	private static boolean hasNoRequirements(ResearchStage stage) {
		return stage.requiredItem().isEmpty()
				&& stage.requiredCraft().isEmpty()
				&& stage.requiredKnowledge().isEmpty()
				&& stage.requiredResearch().isEmpty();
	}

	private static boolean hasStageRequirements(ServerPlayer player, PlayerKnowledge knowledge, ResearchStage stage) {
		for (KnowledgeAmount requirement : stage.requiredKnowledge()) {
			if (knowledge.getKnowledge(requirement.type(), requirement.category()) < requirement.amount()) {
				return false;
			}
		}
		for (ItemRequirement requirement : stage.requiredCraft()) {
			if (!CraftEventRecorder.hasCrafted(player, requirement)) {
				return false;
			}
		}
		for (ResearchRequirement requirement : stage.requiredResearch()) {
			ResearchReference reference = requirement.reference();
			if (!isUnlocked(player, reference.key(), reference.stage())) {
				return false;
			}
		}
		return true;
	}

	private static Map<KnowledgeKey, Long> knowledgeCosts(PlayerKnowledge knowledge, List<KnowledgeAmount> requirements) {
		Map<KnowledgeKey, Long> levelsByKey = new HashMap<>();
		for (KnowledgeAmount requirement : requirements) {
			KnowledgeKey key = new KnowledgeKey(requirement.type(), requirement.category());
			long levels = levelsByKey.getOrDefault(key, 0L) + requirement.amount();
			long raw = levels * requirement.type().progression();
			if (raw > Integer.MAX_VALUE || knowledge.getKnowledgeRaw(key.type(), key.category()) < raw) {
				return null;
			}
			levelsByKey.put(key, levels);
		}
		Map<KnowledgeKey, Long> rawCosts = new HashMap<>();
		levelsByKey.forEach((key, levels) -> rawCosts.put(key, levels * key.type().progression()));
		return rawCosts;
	}

	private static int[] itemCosts(Inventory inventory, List<ItemRequirement> requirements) {
		int[] remaining = new int[inventory.getContainerSize()];
		int[] costs = new int[inventory.getContainerSize()];
		for (int slot = 0; slot < remaining.length; slot++) {
			remaining[slot] = inventory.getItem(slot).getCount();
		}
		for (ItemRequirement requirement : requirements) {
			int missing = requirement.count();
			for (int slot = 0; slot < remaining.length && missing > 0; slot++) {
				ItemStack stack = inventory.getItem(slot);
				if (remaining[slot] == 0 || !matches(requirement, stack)) {
					continue;
				}
				int consumed = Math.min(missing, remaining[slot]);
				remaining[slot] -= consumed;
				costs[slot] += consumed;
				missing -= consumed;
			}
			if (missing > 0) {
				return null;
			}
		}
		return costs;
	}

	private static boolean matches(ItemRequirement requirement, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (requirement.item().isPresent()) {
			return requirement.item().orElseThrow().resolve()
					.map(required -> ItemStack.isSameItemSameComponents(stack, required))
					.orElse(false);
		}
		TagKey<Item> tag = TagKey.create(Registries.ITEM, requirement.tag().orElseThrow());
		return stack.is(tag);
	}

	private static void consumeItems(Inventory inventory, int[] costs) {
		for (int slot = 0; slot < costs.length; slot++) {
			if (costs[slot] > 0) {
				inventory.getItem(slot).shrink(costs[slot]);
			}
		}
		inventory.setChanged();
	}

	private record KnowledgeKey(KnowledgeType type, Optional<Identifier> category) {
	}

	private record KnowledgeCost(KnowledgeKey key, int amount) {
	}

	private static void addResearchWarp(ServerPlayer player, int warp) {
		if (warp > 1) {
			int normal = warp / 2;
			KnowledgeAccess.get().addWarp(player, WarpType.PERMANENT, warp - normal);
			KnowledgeAccess.get().addWarp(player, WarpType.NORMAL, normal);
		} else {
			KnowledgeAccess.get().addWarp(player, WarpType.PERMANENT, warp);
		}
	}

	private static void giveRewards(ServerPlayer player, ResearchEntry entry) {
		for (ItemReference reward : entry.rewardItem()) {
			ItemStack stack = reward.resolve().orElse(null);
			if (stack == null) {
				continue;
			}
			if (!player.getInventory().add(stack)) {
				player.drop(stack, false, Prediction.SERVER_ONLY);
			}
		}
		for (KnowledgeAmount reward : entry.rewardKnowledge()) {
			int raw = (int) Math.min(Integer.MAX_VALUE, (long) reward.amount() * reward.type().progression());
			KnowledgeAccess.get().addKnowledge(player, reward.type(), reward.category(), raw);
		}
	}

	private static void notifyAddenda(ServerPlayer player, Identifier completedKey) {
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		for (var candidate : ResearchAccess.get().entries(false).entrySet()) {
			if (!knowledge.isResearchComplete(candidate.getKey())) {
				continue;
			}
			for (ResearchAddendum addendum : candidate.getValue().addenda()) {
				boolean unlockedByCompletion = addendum.requiredResearch().stream()
						.map(ResearchRequirement::reference)
						.anyMatch(reference -> reference.key().equals(completedKey));
				if (unlockedByCompletion && addendum.requiredResearch().stream()
						.map(ResearchRequirement::reference)
						.allMatch(knowledge::isResearchKnown)) {
					KnowledgeAccess.get().setResearchFlag(player, candidate.getKey(), ResearchFlag.PAGE);
					player.sendSystemMessage(Component.translatable(
							addendum.text(),
							Component.translatable(candidate.getValue().name())
					));
					break;
				}
			}
		}
	}

	private static boolean giveRecursiveResearch(ServerPlayer player, Identifier key, Set<Identifier> visited) {
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		if (knowledge.isResearchComplete(key) || !visited.add(key)) {
			return false;
		}
		boolean changed = false;
		try {
			ResearchEntry entry = ResearchAccess.get().entry(key, false).orElse(null);
			if (entry != null) {
				for (ResearchReference parent : entry.parents()) {
					changed |= giveRecursiveResearch(player, parent.key(), visited);
				}
				for (ResearchStage stage : entry.stages()) {
					for (ResearchRequirement requirement : stage.requiredResearch()) {
						changed |= completeResearch(player, requirement.reference().key(), true);
					}
				}
			}
			changed |= completeResearch(player, key, true);
			for (MapEntry entryWithResearch : entriesRequiring(key)) {
				changed |= KnowledgeAccess.get().setResearchFlag(player, entryWithResearch.key(), ResearchFlag.PAGE);
			}
			if (entry != null) {
				for (ResearchReference sibling : entry.siblings()) {
					changed |= giveRecursiveResearch(player, sibling.key(), visited);
				}
			}
			return changed;
		} finally {
			visited.remove(key);
		}
	}

	private static List<MapEntry> entriesRequiring(Identifier key) {
		return ResearchAccess.get().entries(false).entrySet().stream()
				.filter(entry -> entry.getValue().stages().stream()
						.flatMap(stage -> stage.requiredResearch().stream())
						.anyMatch(requirement -> requirement.reference().key().equals(key)))
				.map(entry -> new MapEntry(entry.getKey()))
				.toList();
	}

	private static boolean revokeRecursiveResearch(ServerPlayer player, Identifier key, Set<Identifier> visited) {
		if (!visited.add(key)) {
			return false;
		}
		try {
			PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
			if (!knowledge.isResearchComplete(key)) {
				return false;
			}
			boolean changed = false;
			for (var candidate : ResearchAccess.get().entries(false).entrySet()) {
				boolean child = candidate.getValue().parents().stream()
						.anyMatch(parent -> parent.key().equals(key));
				if (child && knowledge.isResearchComplete(candidate.getKey())) {
					changed |= revokeRecursiveResearch(player, candidate.getKey(), visited);
				}
			}
			return KnowledgeAccess.get().removeResearch(player, key) || changed;
		} finally {
			visited.remove(key);
		}
	}

	private record MapEntry(Identifier key) {
	}
}
