package io.github.rdarkknight.thaumcraftreborn.systems.research;

import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerKnowledge;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeAmount;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
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
		Optional<ResearchEntry> entry = ResearchAccess.get().entry(key, false);
		if (entry.isEmpty()) {
			return true;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(player);
		return entry.get().parents().stream().allMatch(knowledge::isResearchKnown);
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
			int stage = knowledge.getResearchStage(key);
			ResearchStage currentStage = stage > 0 ? stages.get(Math.min(stage, stages.size()) - 1) : null;
			if (stages.size() == 1 && stage == 0 && hasNoRequirements(stages.getFirst())) {
				stage++;
			} else if (stages.size() > 1 && stages.size() <= stage + 1 && stage < stages.size()
					&& hasNoRequirements(stages.get(stage))) {
				stage++;
			}
			KnowledgeAccess.get().setResearchStage(player, key, Math.min(stages.size() + 1, stage + 1));
			popups = stage >= stages.size();
			int warp = currentStage == null ? 0 : currentStage.warp();
			if (popups) {
				stage = Math.min(stage, stages.size());
				currentStage = stages.get(stage - 1);
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
		for (ItemStack reward : entry.rewardItem()) {
			ItemStack stack = reward.copy();
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
		if (!visited.add(key)) {
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
						changed |= giveRecursiveResearch(player, requirement.reference().key(), visited);
					}
				}
			}
			changed |= completeResearch(player, key, true);
			for (MapEntry entryWithResearch : entriesRequiring(key)) {
				KnowledgeAccess.get().setResearchFlag(player, entryWithResearch.key(), ResearchFlag.PAGE);
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
