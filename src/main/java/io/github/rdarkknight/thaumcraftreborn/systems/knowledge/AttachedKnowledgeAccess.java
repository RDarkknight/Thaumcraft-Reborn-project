package io.github.rdarkknight.thaumcraftreborn.systems.knowledge;

import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerKnowledge;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerWarp;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchReference;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData.KnowledgeKey;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData.ResearchState;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerWarpData;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class AttachedKnowledgeAccess implements KnowledgeAccess {
	public static final AttachedKnowledgeAccess INSTANCE = new AttachedKnowledgeAccess();

	private AttachedKnowledgeAccess() {
	}

	@Override
	public PlayerKnowledge knowledge(Player player) {
		return new AttachedPlayerKnowledge(player);
	}

	@Override
	public PlayerWarp warp(Player player) {
		return new AttachedPlayerWarp(player);
	}

	@Override
	public boolean addResearch(ServerPlayer player, Identifier key) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		ResearchState state = data.research().getOrDefault(key, ResearchState.EMPTY);
		if (state.known()) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withResearch(key, state.withKnown(true)));
		return true;
	}

	@Override
	public boolean removeResearch(ServerPlayer player, Identifier key) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		ResearchState state = data.research().getOrDefault(key, ResearchState.EMPTY);
		if (!state.known()) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withResearch(key, state.withKnown(false)));
		return true;
	}

	@Override
	public boolean setResearchStage(ServerPlayer player, Identifier key, int stage) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		ResearchState state = data.research().getOrDefault(key, ResearchState.EMPTY);
		if (!state.known() || stage <= 0) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withResearch(key, state.withStage(stage)));
		return true;
	}

	@Override
	public boolean setResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		ResearchState state = data.research().getOrDefault(key, ResearchState.EMPTY);
		if (state.flags().contains(flag)) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withResearch(key, state.withFlag(flag, true)));
		return true;
	}

	@Override
	public boolean clearResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		ResearchState state = data.research().getOrDefault(key, ResearchState.EMPTY);
		if (!state.flags().contains(flag)) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withResearch(key, state.withFlag(flag, false)));
		return true;
	}

	@Override
	public boolean addKnowledge(ServerPlayer player, KnowledgeType type, Optional<Identifier> category, int amount) {
		PlayerKnowledgeData data = player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		KnowledgeKey key = new KnowledgeKey(type, category);
		long next = (long) data.knowledge().getOrDefault(key, 0) + amount;
		if (next < 0L || next > Integer.MAX_VALUE) {
			return false;
		}
		player.setAttached(ModAttachments.KNOWLEDGE, data.withKnowledge(key, (int) next));
		return true;
	}

	@Override
	public void clearKnowledge(ServerPlayer player) {
		player.setAttached(ModAttachments.KNOWLEDGE, PlayerKnowledgeData.EMPTY);
	}

	@Override
	public void setWarp(ServerPlayer player, WarpType type, int amount) {
		PlayerWarpData data = player.getAttachedOrCreate(ModAttachments.WARP);
		player.setAttached(ModAttachments.WARP, data.with(type, amount));
	}

	@Override
	public int addWarp(ServerPlayer player, WarpType type, int amount) {
		PlayerWarpData data = player.getAttachedOrCreate(ModAttachments.WARP);
		int next = (int) Math.clamp((long) data.get(type) + amount, 0L, 500L);
		player.setAttached(ModAttachments.WARP, data.with(type, next));
		return next;
	}

	@Override
	public int reduceWarp(ServerPlayer player, WarpType type, int amount) {
		PlayerWarpData data = player.getAttachedOrCreate(ModAttachments.WARP);
		int next = (int) Math.clamp((long) data.get(type) - amount, 0L, 500L);
		player.setAttached(ModAttachments.WARP, data.with(type, next));
		return next;
	}

	@Override
	public void setWarpCounter(ServerPlayer player, int amount) {
		PlayerWarpData data = player.getAttachedOrCreate(ModAttachments.WARP);
		player.setAttached(ModAttachments.WARP, data.withCounter(amount));
	}

	private static final class AttachedPlayerKnowledge implements PlayerKnowledge {
		private final Player player;

		private AttachedPlayerKnowledge(Player player) {
			this.player = player;
		}

		@Override
		public ResearchStatus getResearchStatus(Identifier key) {
			if (!isResearchKnown(key)) {
				return ResearchStatus.UNKNOWN;
			}
			return ResearchAccess.get().entry(key, player.level().isClientSide())
					.filter(entry -> getResearchStage(key) <= entry.stages().size())
					.map(entry -> ResearchStatus.IN_PROGRESS)
					.orElse(ResearchStatus.COMPLETE);
		}

		@Override
		public boolean isResearchKnown(Identifier key) {
			ResearchState state = state(key);
			return state != null && state.known();
		}

		@Override
		public boolean isResearchKnown(ResearchReference reference) {
			if (!isResearchKnown(reference.key())) {
				return false;
			}
			return reference.stage().isEmpty() || getResearchStage(reference.key()) >= reference.stage().getAsInt();
		}

		@Override
		public boolean isResearchComplete(Identifier key) {
			return getResearchStatus(key) == ResearchStatus.COMPLETE;
		}

		@Override
		public int getResearchStage(Identifier key) {
			ResearchState state = state(key);
			return state == null || !state.known() ? -1 : state.stage();
		}

		@Override
		public Set<Identifier> getResearchList() {
			Set<Identifier> keys = new LinkedHashSet<>();
			data().research().forEach((key, state) -> {
				if (state.known()) {
					keys.add(key);
				}
			});
			return Set.copyOf(keys);
		}

		@Override
		public boolean hasResearchFlag(Identifier key, ResearchFlag flag) {
			ResearchState state = state(key);
			return state != null && state.flags().contains(flag);
		}

		@Override
		public int getKnowledge(KnowledgeType type, Optional<Identifier> category) {
			return (int) Math.floor((double) getKnowledgeRaw(type, category) / type.progression());
		}

		@Override
		public int getKnowledgeRaw(KnowledgeType type, Optional<Identifier> category) {
			return data().knowledge().getOrDefault(new KnowledgeKey(type, category), 0);
		}

		private PlayerKnowledgeData data() {
			return player.getAttachedOrCreate(ModAttachments.KNOWLEDGE);
		}

		private ResearchState state(Identifier key) {
			return data().research().get(key);
		}
	}

	private static final class AttachedPlayerWarp implements PlayerWarp {
		private final Player player;

		private AttachedPlayerWarp(Player player) {
			this.player = player;
		}

		@Override
		public int get(WarpType type) {
			return player.getAttachedOrCreate(ModAttachments.WARP).get(type);
		}

		@Override
		public int getCounter() {
			return player.getAttachedOrCreate(ModAttachments.WARP).counter();
		}
	}
}
