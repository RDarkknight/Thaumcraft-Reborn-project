package io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftCard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class ResearchTableData implements TheorycraftCard.TheorycraftSession {
	private static final Codec<Map<String, String>> STRING_MAP = Codec.unboundedMap(Codec.STRING, Codec.STRING);
	public static final Codec<TheorycraftCard.CardOffer> OFFER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("card").forGetter(TheorycraftCard.CardOffer::card),
			Codec.LONG.fieldOf("seed").forGetter(TheorycraftCard.CardOffer::seed),
			STRING_MAP.fieldOf("data").forGetter(TheorycraftCard.CardOffer::data)
	).apply(instance, TheorycraftCard.CardOffer::new));
	private static final Codec<CategoryTotal> TOTAL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("category").forGetter(CategoryTotal::category),
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("total").forGetter(CategoryTotal::total)
	).apply(instance, CategoryTotal::new));
	public static final Codec<ResearchTableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("owner", "").forGetter(data -> data.owner),
			Codec.INT.fieldOf("inspiration").forGetter(data -> data.inspiration),
			Codec.INT.fieldOf("inspiration_start").forGetter(data -> data.inspirationStart),
			TOTAL_CODEC.listOf().optionalFieldOf("category_totals", List.of())
					.forGetter(data -> data.categoryTotals.entrySet().stream()
							.map(entry -> new CategoryTotal(entry.getKey(), entry.getValue())).toList()),
			Codec.INT.optionalFieldOf("bonus_draws", 0).forGetter(data -> data.bonusDraws),
			OFFER_CODEC.listOf().optionalFieldOf("offered_cards", List.of()).forGetter(data -> data.offeredCards),
			Identifier.CODEC.listOf().optionalFieldOf("aids", List.of()).forGetter(data -> data.aids),
			Identifier.CODEC.listOf().optionalFieldOf("aid_cards", List.of()).forGetter(data -> data.aidCards),
			Codec.INT.optionalFieldOf("penalty_start", 0).forGetter(data -> data.penaltyStart),
			Codec.LONG.listOf().optionalFieldOf("saved_cards", List.of()).forGetter(data -> data.savedCards),
			Identifier.CODEC.listOf().optionalFieldOf("blocked_categories", List.of()).forGetter(data -> data.blockedCategories)
	).apply(instance, (owner, inspiration, inspirationStart, totals, bonusDraws, offeredCards, aids,
			aidCards, penaltyStart, savedCards, blockedCategories) -> new ResearchTableData(owner, inspiration,
			inspirationStart, totals, bonusDraws, offeredCards, aids, aidCards, penaltyStart, savedCards,
			blockedCategories)));

	private String owner;
	private int inspiration;
	private int inspirationStart;
	private final Map<Identifier, Integer> categoryTotals = new HashMap<>();
	private int bonusDraws;
	private List<TheorycraftCard.CardOffer> offeredCards = new ArrayList<>();
	private List<Identifier> aids = new ArrayList<>();
	private List<Identifier> aidCards = new ArrayList<>();
	private int penaltyStart;
	private List<Long> savedCards = new ArrayList<>();
	private List<Identifier> blockedCategories = new ArrayList<>();

	private ResearchTableData(
			String owner,
			int inspiration,
			int inspirationStart,
			List<CategoryTotal> totals,
			int bonusDraws,
			List<TheorycraftCard.CardOffer> offeredCards,
			List<Identifier> aids,
			List<Identifier> aidCards,
			int penaltyStart,
			List<Long> savedCards,
			List<Identifier> blockedCategories
	) {
		this.owner = owner;
		this.inspiration = inspiration;
		this.inspirationStart = inspirationStart;
		totals.forEach(total -> categoryTotals.put(total.category(), total.total()));
		this.bonusDraws = bonusDraws;
		this.offeredCards = new ArrayList<>(offeredCards);
		this.aids = new ArrayList<>(aids);
		this.aidCards = new ArrayList<>(aidCards);
		this.penaltyStart = penaltyStart;
		this.savedCards = new ArrayList<>(savedCards);
		this.blockedCategories = new ArrayList<>(blockedCategories);
	}

	public ResearchTableData() {
		this("", 0, 0, List.of(), 0, List.of(), List.of(), List.of(), 0, List.of(), List.of());
	}

	private record CategoryTotal(Identifier category, int total) {
	}

	public void start(String player, int inspiration, List<Identifier> aids) {
		this.owner = player;
		this.inspirationStart = Math.min(15, Math.max(0, inspiration));
		this.inspiration = this.inspirationStart - aids.size();
		this.aids = new ArrayList<>(aids);
		this.aidCards.clear();
		this.categoryTotals.clear();
		this.offeredCards.clear();
		this.savedCards.clear();
		this.blockedCategories.clear();
		this.bonusDraws = 0;
		this.penaltyStart = 0;
	}

	public String owner() { return owner; }
	public int inspiration() { return inspiration; }
	public int inspirationStart() { return inspirationStart; }
	@Override
	public Map<Identifier, Integer> categoryTotals() { return Map.copyOf(categoryTotals); }
	public int bonusDraws() { return bonusDraws; }
	public List<TheorycraftCard.CardOffer> offeredCards() { return List.copyOf(offeredCards); }
	public List<Identifier> aids() { return List.copyOf(aids); }
	public List<Identifier> aidCards() { return List.copyOf(aidCards); }
	public int penaltyStart() { return penaltyStart; }
	public List<Long> savedCards() { return List.copyOf(savedCards); }
	@Override
	public List<Identifier> blockedCategories() { return List.copyOf(blockedCategories); }
	public boolean isActive() { return !owner.isEmpty() && inspiration > 0; }
	public void setInspiration(int value) { inspiration = value; }
	public void setOfferedCards(List<TheorycraftCard.CardOffer> cards) { offeredCards = new ArrayList<>(cards); }
	public void addSavedCard(long seed) { savedCards.add(seed); }
	public void setAidCards(List<Identifier> cards) { aidCards = new ArrayList<>(cards); }
	public boolean removeAidCard(Identifier card) { return aidCards.remove(card); }
	public void clearSession() { owner = ""; inspiration = 0; offeredCards.clear(); }

	@Override
	public int total(Identifier category) {
		return categoryTotals.getOrDefault(category, 0);
	}

	@Override
	public void addTotal(Identifier category, int amount) {
		int total = categoryTotals.getOrDefault(category, 0) + amount;
		if (total <= 0) categoryTotals.remove(category);
		else categoryTotals.put(category, total);
	}

	@Override
	public void addInspiration(int amount) {
		inspiration = Math.min(inspirationStart, inspiration + amount);
	}

	@Override
	public void addBonusDraws(int amount) {
		bonusDraws = Math.max(0, bonusDraws + amount);
	}

	@Override
	public void addPenaltyStart(int amount) {
		penaltyStart += amount;
	}

	@Override
	public void blockCategory(Identifier category) {
		if (!blockedCategories.contains(category)) blockedCategories.add(category);
	}
}
