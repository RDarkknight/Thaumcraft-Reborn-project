package io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftCard;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

final class BuiltinTheorycraftCard implements TheorycraftCard {
	private static final Identifier BASICS = id("basics");
	private static final Identifier ALCHEMY = id("alchemy");
	private final Identifier id;
	private final boolean aidOnly;

	BuiltinTheorycraftCard(String path, boolean aidOnly) {
		this.id = id(path);
		this.aidOnly = aidOnly;
	}

	@Override
	public Identifier id() {
		return id;
	}

	@Override
	public boolean aidOnly() {
		return aidOnly;
	}

	@Override
	public Optional<CardOffer> createOffer(CardContext context, RandomSource random) {
		Map<String, String> data = new HashMap<>();
		switch (id.getPath()) {
			case "study" -> {
				if (context.categories().isEmpty()) return Optional.empty();
				data.put("category", context.categories().get(random.nextInt(context.categories().size())).toString());
			}
			case "analyze" -> {
				List<Identifier> candidates = context.categories().stream()
						.filter(category -> !category.equals(BASICS))
						.filter(category -> KnowledgeAccess.get().knowledge(context.player())
								.getKnowledge(KnowledgeType.OBSERVATION, Optional.of(category)) > 0)
						.toList();
				if (candidates.isEmpty()) return Optional.empty();
				data.put("category", candidates.get(random.nextInt(candidates.size())).toString());
			}
			case "balance" -> {
				long active = context.categoryTotals().keySet().stream().filter(c -> !context.blockedCategories().contains(c)).count();
				int total = context.categoryTotals().entrySet().stream()
						.filter(entry -> !context.blockedCategories().contains(entry.getKey()))
						.mapToInt(Map.Entry::getValue).sum();
				if (active == 0 || context.blockedCategories().size() >= context.categoryTotals().size() - 1 || total < active) {
					return Optional.empty();
				}
			}
			case "notation" -> {
				if (context.categoryTotals().size() < 2) return Optional.empty();
				var low = context.categoryTotals().entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow();
				var high = context.categoryTotals().entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
				if (low.getValue() <= 0 || high.getValue() <= low.getValue()) return Optional.empty();
				data.put("low", low.getKey().toString());
				data.put("high", high.getKey().toString());
			}
			case "ponder" -> {
				if (context.blockedCategories().size() >= context.categoryTotals().size()) return Optional.empty();
			}
			case "rethink" -> {
				if (context.categoryTotals().values().stream().mapToInt(Integer::intValue).sum() < 10) return Optional.empty();
			}
			case "reject" -> {
				List<Identifier> candidates = context.categoryTotals().keySet().stream()
						.filter(category -> !context.blockedCategories().contains(category)).toList();
				if (candidates.isEmpty()) return Optional.empty();
				data.put("category", candidates.get(random.nextInt(candidates.size())).toString());
			}
			case "inspired" -> {
				if (context.categoryTotals().isEmpty()) return Optional.empty();
				var highest = context.categoryTotals().entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
				data.put("category", highest.getKey().toString());
				data.put("amount", Integer.toString(10 + highest.getValue() / 2));
			}
			case "concentrate" -> {
				List<Aspect> compounds = Aspect.registry().entrySet().stream()
						.map(Map.Entry::getValue).filter(aspect -> !aspect.isPrimal()).toList();
				if (compounds.isEmpty()) return Optional.empty();
				Aspect first = compounds.get(random.nextInt(compounds.size()));
				data.put("aspect", aspectId(first).toString());
			}
			case "reactions" -> {
				List<Aspect> compounds = Aspect.registry().entrySet().stream()
						.map(Map.Entry::getValue).filter(aspect -> !aspect.isPrimal()).toList();
				if (compounds.size() < 2) return Optional.empty();
				Aspect first = compounds.get(random.nextInt(compounds.size()));
				List<Aspect> others = compounds.stream().filter(aspect -> !aspect.equals(first)).toList();
				Aspect second = others.get(random.nextInt(others.size()));
				data.put("aspect", aspectId(first).toString());
				data.put("aspect2", aspectId(second).toString());
			}
			case "synthesis" -> {
				List<Aspect> compounds = Aspect.registry().entrySet().stream()
						.map(Map.Entry::getValue).filter(aspect -> aspect.components().size() == 2).toList();
				if (compounds.isEmpty()) return Optional.empty();
				Aspect result = compounds.get(random.nextInt(compounds.size()));
				data.put("aspect", aspectId(result.components().get(0)).toString());
				data.put("aspect2", aspectId(result.components().get(1)).toString());
				data.put("result", aspectId(result).toString());
			}
			case "experimentation" -> {
				if (context.categories().isEmpty()) return Optional.empty();
			}
			default -> {
				return Optional.empty();
			}
		}
		return Optional.of(new CardOffer(id, random.nextLong(), data));
	}

	@Override
	public int inspirationCost(CardOffer offer) {
		return switch (id.getPath()) {
			case "reject" -> 0;
			case "study", "balance", "notation", "concentrate", "reactions", "synthesis" -> 1;
			case "analyze", "ponder", "experimentation", "inspired" -> 2;
			case "rethink" -> -1;
			default -> Integer.MAX_VALUE;
		};
	}

	@Override
	public Optional<Identifier> category(CardOffer offer) {
		return switch (id.getPath()) {
			case "study", "analyze", "inspired" -> Optional.ofNullable(offer.data().get("category")).map(Identifier::parse);
			case "concentrate", "reactions", "synthesis" -> Optional.of(ALCHEMY);
			default -> Optional.empty();
		};
	}

	@Override
	public List<ItemStack> requiredItems(CardOffer offer) {
		List<String> keys = switch (id.getPath()) {
			case "concentrate" -> List.of("aspect");
			case "reactions", "synthesis" -> List.of("aspect", "aspect2");
			default -> List.of();
		};
		List<ItemStack> result = new ArrayList<>();
		for (String key : keys) {
			Identifier aspectId = Identifier.tryParse(offer.data().getOrDefault(key, ""));
			if (aspectId == null) return List.of(ItemStack.EMPTY);
			Aspect aspect = Aspect.registry().getValue(aspectId);
			if (aspect == null) return List.of(ItemStack.EMPTY);
			ItemStack essence = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
			essence.set(ModDataComponents.CRYSTAL_ASPECT, aspect);
			result.add(essence);
		}
		return result;
	}

	@Override
	public List<Boolean> consumedItems(CardOffer offer) {
		return switch (id.getPath()) {
			case "concentrate", "reactions" -> List.of(false, false).subList(0, requiredItems(offer).size());
			case "synthesis" -> List.of(true, true);
			default -> List.of();
		};
	}

	@Override
	public Component name(CardOffer offer) {
		return Component.translatable("card.thaumcraft_reborn." + id.getPath() + ".name");
	}

	@Override
	public Component description(CardOffer offer) {
		return Component.translatable("card.thaumcraft_reborn." + id.getPath() + ".description");
	}

	@Override
	public boolean activate(ServerPlayer player, TheorycraftSession session, CardOffer offer) {
		return activate(player, session, offer, player.getRandom());
	}

	@Override
	public boolean activate(ServerPlayer player, TheorycraftSession session, CardOffer offer, RandomSource random) {
		switch (id.getPath()) {
			case "study" -> session.addTotal(Identifier.parse(offer.data().get("category")), between(random, 15, 25));
			case "analyze" -> {
				Identifier category = Identifier.parse(offer.data().get("category"));
				int observation = KnowledgeAccess.get().knowledge(player)
						.getKnowledgeRaw(KnowledgeType.OBSERVATION, Optional.of(category));
				int progression = KnowledgeType.OBSERVATION.progression();
				if (observation < progression) return false;
				KnowledgeAccess.get().addKnowledge(player, KnowledgeType.OBSERVATION, Optional.of(category), -progression);
				session.addTotal(BASICS, 5);
				session.addTotal(category, between(random, 25, 50));
			}
			case "balance" -> {
				Map<Identifier, Integer> totals = session.categoryTotals();
				List<Identifier> active = totals.keySet().stream()
						.filter(category -> !session.blockedCategories().contains(category)).toList();
				if (active.isEmpty()) return false;
				int sum = active.stream().mapToInt(session::total).sum();
				for (Identifier category : active) session.addTotal(category, sum / active.size() - session.total(category));
				session.addTotal(BASICS, 5);
				session.addPenaltyStart(1);
			}
			case "notation" -> {
				Identifier low = Identifier.parse(offer.data().get("low"));
				Identifier high = Identifier.parse(offer.data().get("high"));
				int value = session.total(low);
				if (value <= 0) return false;
				session.addTotal(low, -value);
				session.addTotal(high, value / 2 + random.nextInt(value / 2 + 1));
			}
			case "ponder" -> {
				Map<Identifier, Integer> totals = session.categoryTotals();
				for (int remaining = 25; remaining > 0;) {
					for (Identifier category : totals.keySet()) {
						if (session.blockedCategories().contains(category)) continue;
						session.addTotal(category, 1);
						if (--remaining == 0) break;
					}
					if (totals.keySet().stream().allMatch(session.blockedCategories()::contains)) return false;
				}
				session.addTotal(BASICS, 5);
				session.addBonusDraws(1);
			}
			case "rethink" -> {
				int remaining = Math.min(10, session.categoryTotals().values().stream().mapToInt(Integer::intValue).sum());
				while (remaining > 0) {
					boolean changed = false;
					for (Identifier category : session.categoryTotals().keySet()) {
						if (session.total(category) > 0) {
							session.addTotal(category, -1);
							remaining--;
							changed = true;
							if (remaining == 0) break;
						}
					}
					if (!changed) break;
				}
				session.addBonusDraws(1);
				session.addTotal(BASICS, between(random, 1, 10));
			}
			case "reject" -> {
				Identifier category = Identifier.parse(offer.data().get("category"));
				session.addTotal(BASICS, 5);
				session.blockCategory(category);
			}
			case "experimentation" -> {
				List<Identifier> categories = ResearchAccess.get().categoryOrder(false);
				if (categories.isEmpty()) return false;
				session.addTotal(categories.get(random.nextInt(categories.size())), between(random, 15, 30));
				session.addTotal(BASICS, between(random, 1, 10));
			}
			case "inspired" -> session.addTotal(
					Identifier.parse(offer.data().get("category")), Integer.parseInt(offer.data().get("amount")));
			case "concentrate" -> {
				session.addTotal(ALCHEMY, 15);
				session.addBonusDraws(1);
				if (random.nextFloat() <= 0.33F) session.addInspiration(1);
			}
			case "reactions" -> {
				session.addTotal(ALCHEMY, 25);
				if (random.nextFloat() <= 0.33F) session.addInspiration(1);
			}
			case "synthesis" -> {
				session.addTotal(ALCHEMY, 40);
				if (random.nextFloat() <= 0.33F) session.addInspiration(1);
				Aspect result = Aspect.registry().getValue(Identifier.parse(offer.data().getOrDefault("result", "")));
				if (result == null) return false;
				ItemStack essence = new ItemStack(ThaumcraftContent.CRYSTAL_ESSENCE);
				essence.set(ModDataComponents.CRYSTAL_ASPECT, result);
				if (!essence.isEmpty()) {
						if (!player.getInventory().add(essence.copy())) player.drop(essence.copy(), false, Prediction.SERVER_ONLY);
				}
			}
			default -> {
				return false;
			}
		}
		return true;
	}

	private static int between(RandomSource random, int minimum, int maximum) {
		return random.nextInt(maximum - minimum + 1) + minimum;
	}

	private static Identifier aspectId(Aspect aspect) {
		return Aspect.registry().getKey(aspect);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("thaumcraft_reborn", path);
	}
}
