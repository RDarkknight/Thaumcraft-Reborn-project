package io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface TheorycraftCard {
	Identifier id();

	boolean aidOnly();

	Optional<CardOffer> createOffer(CardContext context, RandomSource random);

	int inspirationCost(CardOffer offer);

	Optional<Identifier> category(CardOffer offer);

	List<ItemStack> requiredItems(CardOffer offer);

	List<Boolean> consumedItems(CardOffer offer);

	Component name(CardOffer offer);

	Component description(CardOffer offer);

	boolean activate(ServerPlayer player, TheorycraftSession session, CardOffer offer);

	default boolean activate(ServerPlayer player, TheorycraftSession session, CardOffer offer, RandomSource random) {
		return activate(player, session, offer);
	}

	record CardContext(
			ServerPlayer player,
			int inspiration,
			Map<Identifier, Integer> categoryTotals,
			List<Identifier> categories,
			List<Identifier> blockedCategories
	) {
		public CardContext {
			categoryTotals = Map.copyOf(categoryTotals);
			categories = List.copyOf(categories);
			blockedCategories = List.copyOf(blockedCategories);
		}
	}

	record CardOffer(Identifier card, long seed, Map<String, String> data) {
		public CardOffer {
			data = Map.copyOf(data);
		}
	}

	interface TheorycraftSession {
		int total(Identifier category);

		Map<Identifier, Integer> categoryTotals();

		List<Identifier> blockedCategories();

		void addTotal(Identifier category, int amount);

		void addInspiration(int amount);

		void addBonusDraws(int amount);

		void addPenaltyStart(int amount);

		void blockCategory(Identifier category);
	}
}
