package io.github.rdarkknight.thaumcraftreborn.api.knowledge;

import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface KnowledgeAccess {
	KnowledgeAccess NONE = new KnowledgeAccess() {
		@Override
		public PlayerKnowledge knowledge(Player player) {
			return PlayerKnowledge.EMPTY;
		}

		@Override
		public PlayerWarp warp(Player player) {
			return PlayerWarp.EMPTY;
		}

		@Override
		public boolean addResearch(ServerPlayer player, Identifier key) {
			return false;
		}

		@Override
		public boolean removeResearch(ServerPlayer player, Identifier key) {
			return false;
		}

		@Override
		public boolean setResearchStage(ServerPlayer player, Identifier key, int stage) {
			return false;
		}

		@Override
		public boolean setResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag) {
			return false;
		}

		@Override
		public boolean clearResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag) {
			return false;
		}

		@Override
		public boolean addKnowledge(ServerPlayer player, KnowledgeType type, Optional<Identifier> category, int amount) {
			return false;
		}

		@Override
		public void clearKnowledge(ServerPlayer player) {
		}

		@Override
		public void setWarp(ServerPlayer player, WarpType type, int amount) {
		}

		@Override
		public int addWarp(ServerPlayer player, WarpType type, int amount) {
			return 0;
		}

		@Override
		public int reduceWarp(ServerPlayer player, WarpType type, int amount) {
			return 0;
		}

		@Override
		public void setWarpCounter(ServerPlayer player, int amount) {
		}
	};

	static KnowledgeAccess get() {
		return Holder.INSTANCE;
	}

	static void install(KnowledgeAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	PlayerKnowledge knowledge(Player player);

	PlayerWarp warp(Player player);

	boolean addResearch(ServerPlayer player, Identifier key);

	boolean removeResearch(ServerPlayer player, Identifier key);

	boolean setResearchStage(ServerPlayer player, Identifier key, int stage);

	boolean setResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag);

	boolean clearResearchFlag(ServerPlayer player, Identifier key, ResearchFlag flag);

	boolean addKnowledge(ServerPlayer player, KnowledgeType type, Optional<Identifier> category, int amount);

	void clearKnowledge(ServerPlayer player);

	void setWarp(ServerPlayer player, WarpType type, int amount);

	int addWarp(ServerPlayer player, WarpType type, int amount);

	int reduceWarp(ServerPlayer player, WarpType type, int amount);

	void setWarpCounter(ServerPlayer player, int amount);

	final class Holder {
		private static volatile KnowledgeAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
