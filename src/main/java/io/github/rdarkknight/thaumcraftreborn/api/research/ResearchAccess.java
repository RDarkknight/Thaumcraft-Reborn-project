package io.github.rdarkknight.thaumcraftreborn.api.research;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface ResearchAccess {
	ResearchAccess NONE = new ResearchAccess() {
		@Override
		public Map<Identifier, ResearchEntry> entries(boolean clientSide) {
			return Map.of();
		}

		@Override
		public Map<Identifier, ResearchCategory> categories(boolean clientSide) {
			return Map.of();
		}

		@Override
		public List<Identifier> categoryOrder(boolean clientSide) {
			return List.of();
		}

		@Override
		public List<Identifier> entriesInCategory(Identifier category, boolean clientSide) {
			return List.of();
		}

		@Override
		public Optional<ResearchBounds> bounds(Identifier category, boolean clientSide) {
			return Optional.empty();
		}

		@Override
		public boolean progressResearch(ServerPlayer player, Identifier key, boolean sync) {
			return false;
		}

		@Override
		public boolean completeResearch(ServerPlayer player, Identifier key, boolean sync) {
			return false;
		}

		@Override
		public boolean giveRecursiveResearch(ServerPlayer player, Identifier key) {
			return false;
		}

		@Override
		public boolean revokeRecursiveResearch(ServerPlayer player, Identifier key) {
			return false;
		}

		@Override
		public boolean doesPlayerHaveRequisites(Player player, Identifier key) {
			return true;
		}
	};

	Map<Identifier, ResearchEntry> entries(boolean clientSide);

	Map<Identifier, ResearchCategory> categories(boolean clientSide);

	List<Identifier> categoryOrder(boolean clientSide);

	List<Identifier> entriesInCategory(Identifier category, boolean clientSide);

	Optional<ResearchBounds> bounds(Identifier category, boolean clientSide);

	boolean progressResearch(ServerPlayer player, Identifier key, boolean sync);

	boolean completeResearch(ServerPlayer player, Identifier key, boolean sync);

	boolean giveRecursiveResearch(ServerPlayer player, Identifier key);

	boolean revokeRecursiveResearch(ServerPlayer player, Identifier key);

	boolean doesPlayerHaveRequisites(Player player, Identifier key);

	default Optional<ResearchEntry> entry(Identifier key, boolean clientSide) {
		return Optional.ofNullable(entries(clientSide).get(key));
	}

	default Optional<ResearchCategory> category(Identifier key, boolean clientSide) {
		return Optional.ofNullable(categories(clientSide).get(key));
	}

	static ResearchAccess get() {
		return Holder.INSTANCE;
	}

	static void install(ResearchAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	final class Holder {
		private static volatile ResearchAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
