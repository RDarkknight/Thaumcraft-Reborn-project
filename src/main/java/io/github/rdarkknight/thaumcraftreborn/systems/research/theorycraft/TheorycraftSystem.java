package io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft;

import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchMeta;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftAid;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftCard;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.ResearchTableData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class TheorycraftSystem {
	private static final Identifier BASICS = id("basics");
	private static final Identifier ALCHEMY = id("alchemy");
	private static final Identifier UNLOCK_ALCHEMY = id("unlockalchemy");
	private static final TagKey<net.minecraft.world.level.block.Block> BOOKSHELVES =
			TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "bookshelves"));
	private static final Map<Identifier, TheorycraftCard> CARDS = new HashMap<>();
	private static final Map<Identifier, TheorycraftAid> AIDS = new HashMap<>();
	private static final Map<java.util.UUID, RandomSource> TEST_SESSION_RANDOM = new java.util.concurrent.ConcurrentHashMap<>();

	private TheorycraftSystem() {
	}

	public static void seedSessionRandomForTesting(ServerPlayer player, long seed) {
		TEST_SESSION_RANDOM.put(player.getUUID(), RandomSource.create(seed));
	}

	public static void clearSessionRandomForTesting(ServerPlayer player) {
		TEST_SESSION_RANDOM.remove(player.getUUID());
	}

	private static RandomSource sessionRandom(ServerPlayer player) {
		return TEST_SESSION_RANDOM.getOrDefault(player.getUUID(), player.getRandom());
	}

	public static void init() {
		TheorycraftAccess.install(new TheorycraftAccess() {
			@Override
			public void registerCard(TheorycraftCard card) {
				CARDS.put(card.id(), card);
			}

			@Override
			public void registerAid(TheorycraftAid aid) {
				AIDS.put(aid.id(), aid);
			}

			@Override
			public Optional<TheorycraftCard> card(Identifier id) {
				return Optional.ofNullable(CARDS.get(id));
			}

			@Override
			public Optional<TheorycraftAid> aid(Identifier id) {
				return Optional.ofNullable(AIDS.get(id));
			}

			@Override
			public java.util.Collection<TheorycraftCard> cards() {
				return List.copyOf(CARDS.values());
			}

			@Override
			public java.util.Collection<TheorycraftAid> aids() {
				return List.copyOf(AIDS.values());
			}
		});
		for (String path : List.of("study", "analyze", "balance", "notation", "ponder", "rethink",
				"reject", "experimentation", "inspired", "concentrate", "reactions", "synthesis")) {
			TheorycraftAccess.get().registerCard(new BuiltinTheorycraftCard(path,
					path.equals("study") || path.equals("notation")));
		}
		TheorycraftAccess.get().registerAid(new BasicAlchemyAid());
		TheorycraftAccess.get().registerAid(new BookshelfAid());
	}

	public static List<Identifier> availableCategories(ServerPlayer player) {
		List<Identifier> categories = new ArrayList<>(List.of(BASICS));
		if (ResearchAccess.get().category(ALCHEMY, false).isPresent()
				&& ResearchProgression.isUnlocked(player, UNLOCK_ALCHEMY, OptionalInt.empty())) {
			categories.add(ALCHEMY);
		}
		return categories;
	}

	public static boolean start(ResearchTableBlockEntity table, ServerPlayer player) {
		ItemStack tools = table.getItem(0);
		ItemStack paper = table.getItem(1);
		if (table.data().isActive() || tools.isEmpty() || !tools.is(ThaumcraftContent.SCRIBING_TOOLS)
				|| tools.getDamageValue() >= tools.getMaxDamage()
				|| paper.isEmpty() || !paper.is(Items.PAPER)) {
			return false;
		}
		int spiky = 0;
		int hidden = 0;
		for (Identifier key : KnowledgeAccess.get().knowledge(player).getResearchList()) {
			if (!ResearchProgression.isUnlocked(player, key, OptionalInt.empty())) continue;
			var entry = ResearchAccess.get().entry(key, false).orElse(null);
			if (entry == null) continue;
			if (entry.meta().contains(ResearchMeta.SPIKY)) spiky++;
			if (entry.meta().contains(ResearchMeta.HIDDEN)) hidden++;
		}
		int inspiration = Math.min(15, Math.round(5.0F + 0.5F * spiky + 0.1F * hidden));
		List<Identifier> aids = findAids(table);
		table.data().start(player.getUUID().toString(), inspiration, aids);
		List<Identifier> aidCards = new ArrayList<>();
		for (Identifier aid : aids) {
			TheorycraftAccess.get().aid(aid).ifPresent(definition -> aidCards.addAll(definition.cards()));
		}
		table.data().setAidCards(aidCards);
		table.markDirtyAndSync();
		return true;
	}

	public static boolean draw(ResearchTableBlockEntity table, ServerPlayer player, int requested) {
		ResearchTableData data = table.data();
		if (!ownsActiveSession(data, player) || data.inspiration() <= 0 || requested != 2 && requested != 3) return false;
		if (requested == 3 && data.bonusDraws() <= 0) requested = 2;
		ItemStack paper = table.getItem(1);
		if (paper.isEmpty() || !paper.is(Items.PAPER)) return false;
		paper.shrink(1);

		List<Identifier> availableCategories = availableCategories(player).stream()
				.filter(category -> !data.blockedCategories().contains(category)).toList();
		TheorycraftCard.CardContext context = new TheorycraftCard.CardContext(
				player, data.inspiration(), data.categoryTotals(), availableCategories, data.blockedCategories());
		List<TheorycraftCard.CardOffer> offers = new ArrayList<>();
		Set<Identifier> alreadyDrawn = new HashSet<>();
		int attempts = 0;
		while (offers.size() < requested && attempts++ < 500) {
			RandomSource random = sessionRandom(player);
			boolean aidDraw = !data.aidCards().isEmpty() && random.nextFloat() <= 0.25F;
			Identifier chosen;
			if (aidDraw) {
				List<Identifier> possible = data.aidCards().stream().filter(card -> !alreadyDrawn.contains(card)).toList();
				if (possible.isEmpty()) continue;
				chosen = possible.get(random.nextInt(possible.size()));
			} else {
				List<TheorycraftCard> possible = TheorycraftAccess.get().cards().stream()
						.filter(card -> !card.aidOnly() && !alreadyDrawn.contains(card.id())).toList();
				if (possible.isEmpty()) break;
				chosen = possible.get(random.nextInt(possible.size())).id();
			}
			TheorycraftCard card = TheorycraftAccess.get().card(chosen).orElse(null);
			if (card == null || !aidDraw && card.aidOnly()) continue;
			Optional<TheorycraftCard.CardOffer> offer = card.createOffer(context, random);
			if (offer.isEmpty() || card.inspirationCost(offer.get()) > data.inspiration()) continue;
			Optional<Identifier> category = card.category(offer.get());
			if (category.isPresent() && !availableCategories.contains(category.get())) continue;
			offers.add(offer.get());
			alreadyDrawn.add(chosen);
			if (aidDraw) data.removeAidCard(chosen);
		}
		if (offers.isEmpty()) {
			paper.grow(1);
			return false;
		}
		if (requested == 3 && data.bonusDraws() > 0) data.addBonusDraws(-1);
		data.setOfferedCards(offers);
		table.markDirtyAndSync();
		return true;
	}

	public static boolean pick(ResearchTableBlockEntity table, ServerPlayer player, int index) {
		ResearchTableData data = table.data();
		if (!ownsActiveSession(data, player) || index < 0 || index >= data.offeredCards().size()) return false;
		TheorycraftCard.CardOffer offer = data.offeredCards().get(index);
		TheorycraftCard card = TheorycraftAccess.get().card(offer.card()).orElse(null);
		if (card == null || card.inspirationCost(offer) > data.inspiration()) return false;
		List<ItemStack> required = card.requiredItems(offer);
		List<Boolean> consumed = card.consumedItems(offer);
		if (!hasItems(player, required)) return false;
		if (!card.activate(player, data, offer, sessionRandom(player))) return false;
		for (int i = 0; i < required.size(); i++) {
			if (i < consumed.size() && consumed.get(i)) consume(player, required.get(i).copy());
		}
		data.addInspiration(-card.inspirationCost(offer));
		ItemStack tools = table.getItem(0);
		if (!tools.isEmpty() && tools.is(ThaumcraftContent.SCRIBING_TOOLS) && tools.getDamageValue() < tools.getMaxDamage()) {
			tools.setDamageValue(tools.getDamageValue() + 1);
			if (tools.getDamageValue() >= tools.getMaxDamage()) table.setItem(0, ItemStack.EMPTY);
		}
		data.addSavedCard(offer.seed());
		data.setOfferedCards(List.of());
		if (data.inspiration() <= 0) finish(player, data);
		table.markDirtyAndSync();
		return true;
	}

	private static void finish(ServerPlayer player, ResearchTableData data) {
		List<Map.Entry<Identifier, Integer>> totals = data.categoryTotals().entrySet().stream()
				.sorted(Map.Entry.<Identifier, Integer>comparingByValue().reversed()).toList();
		for (int index = 0; index < totals.size(); index++) {
			Map.Entry<Identifier, Integer> entry = totals.get(index);
			int amount = Math.round(entry.getValue() / 100.0F * KnowledgeType.THEORY.progression());
			if (index > data.penaltyStart()) amount = (int) Math.max(1.0, amount * 0.666666667);
			KnowledgeAccess.get().addKnowledge(player, KnowledgeType.THEORY, Optional.of(entry.getKey()), amount);
		}
		data.clearSession();
	}

	private static boolean ownsActiveSession(ResearchTableData data, ServerPlayer player) {
		return data.isActive() && data.owner().equals(player.getUUID().toString());
	}

	private static boolean hasItems(ServerPlayer player, List<ItemStack> required) {
		Map<ItemStack, Integer> needed = new HashMap<>();
		for (ItemStack stack : required) {
			if (stack.isEmpty()) return false;
			needed.merge(stack.copyWithCount(1), stack.getCount(), Integer::sum);
		}
		for (Map.Entry<ItemStack, Integer> entry : needed.entrySet()) {
			int count = 0;
			for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
				ItemStack inventoryStack = player.getInventory().getItem(slot);
				if (ItemStack.isSameItemSameComponents(inventoryStack, entry.getKey())) count += inventoryStack.getCount();
			}
			if (count < entry.getValue()) return false;
		}
		return true;
	}

	private static void consume(ServerPlayer player, ItemStack required) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!ItemStack.isSameItemSameComponents(stack, required)) continue;
			int removed = Math.min(required.getCount(), stack.getCount());
			stack.shrink(removed);
			required.shrink(removed);
			if (required.isEmpty()) break;
		}
		player.getInventory().setChanged();
	}

	private static List<Identifier> findAids(ResearchTableBlockEntity table) {
		List<Identifier> aids = new ArrayList<>();
		BlockPos origin = table.getBlockPos();
		for (int y = -1; y <= 1; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					BlockPos pos = origin.offset(x, y, z);
					for (TheorycraftAid aid : TheorycraftAccess.get().aids()) {
						if (aid.matches(table.getLevel(), pos) && !aids.contains(aid.id())) aids.add(aid.id());
					}
				}
			}
		}
		return aids;
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("thaumcraft_reborn", path);
	}

	private static final class BookshelfAid implements TheorycraftAid {
		@Override
		public Identifier id() {
			return TheorycraftSystem.id("bookshelf");
		}

		@Override
		public boolean matches(net.minecraft.world.level.Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			return state.is(Blocks.BOOKSHELF) || state.is(BOOKSHELVES);
		}

		@Override
		public List<Identifier> cards() {
			return List.of(TheorycraftSystem.id("balance"), TheorycraftSystem.id("notation"),
					TheorycraftSystem.id("notation"), TheorycraftSystem.id("study"),
					TheorycraftSystem.id("study"), TheorycraftSystem.id("study"));
		}
	}

	private static final class BasicAlchemyAid implements TheorycraftAid {
		@Override
		public Identifier id() {
			return TheorycraftSystem.id("basic_alchemy");
		}

		@Override
		public boolean matches(net.minecraft.world.level.Level level, BlockPos pos) {
			return level.getBlockState(pos).is(ThaumcraftContent.CRUCIBLE);
		}

		@Override
		public List<Identifier> cards() {
			return List.of(TheorycraftSystem.id("concentrate"), TheorycraftSystem.id("reactions"),
					TheorycraftSystem.id("synthesis"));
		}
	}
}
