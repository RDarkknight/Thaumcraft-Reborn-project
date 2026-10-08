package io.github.rdarkknight.thaumcraftreborn.systems.research;

import com.mojang.logging.LogUtils;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.item.ItemReference;
import io.github.rdarkknight.thaumcraftreborn.api.research.ItemRequirement;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchBounds;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchCategory;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchEntry;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchIcon.Item;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStage;
import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public final class ResearchIndex implements ResearchAccess {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final SyncedDataLoader<ResearchEntry> RESEARCH = new SyncedDataLoader<>(
			ThaumcraftRebornApi.id("research"),
			ResearchEntry.CODEC,
			ByteBufCodecs.fromCodecWithRegistries(ResearchEntry.CODEC)
	);
	public static final SyncedDataLoader<ResearchCategory> CATEGORIES = new SyncedDataLoader<>(
			ThaumcraftRebornApi.id("research_categories"),
			ResearchCategory.CODEC,
			ByteBufCodecs.fromCodecWithRegistries(ResearchCategory.CODEC)
	);
	private static final ResearchIndex INSTANCE = new ResearchIndex();
	private static volatile Snapshot serverSnapshot = Snapshot.EMPTY;
	private static volatile Snapshot clientSnapshot = Snapshot.EMPTY;
	private static volatile int unresolvedItemReferenceCount;

	static {
		RESEARCH.addServerEntriesListener(ResearchIndex::rebuildServer);
		CATEGORIES.addServerEntriesListener(ResearchIndex::rebuildServer);
		RESEARCH.addClientEntriesListener(ResearchIndex::rebuildClient);
		CATEGORIES.addClientEntriesListener(ResearchIndex::rebuildClient);
	}

	private ResearchIndex() {
	}

	public static void init() {
		ResearchAccess.install(INSTANCE);
	}

	public static int unresolvedItemReferenceCount() {
		return unresolvedItemReferenceCount;
	}

	@Override
	public Map<Identifier, ResearchEntry> entries(boolean clientSide) {
		return snapshot(clientSide).entries;
	}

	@Override
	public Map<Identifier, ResearchCategory> categories(boolean clientSide) {
		return snapshot(clientSide).categories;
	}

	@Override
	public List<Identifier> categoryOrder(boolean clientSide) {
		return snapshot(clientSide).categoryOrder;
	}

	@Override
	public List<Identifier> entriesInCategory(Identifier category, boolean clientSide) {
		return snapshot(clientSide).entriesByCategory.getOrDefault(category, List.of());
	}

	@Override
	public Optional<ResearchBounds> bounds(Identifier category, boolean clientSide) {
		return Optional.ofNullable(snapshot(clientSide).bounds.get(category));
	}

	@Override
	public boolean progressResearch(ServerPlayer player, Identifier key, boolean sync) {
		return ResearchProgression.progressResearch(player, key, sync);
	}

	@Override
	public boolean completeResearch(ServerPlayer player, Identifier key, boolean sync) {
		return ResearchProgression.completeResearch(player, key, sync);
	}

	@Override
	public boolean giveRecursiveResearch(ServerPlayer player, Identifier key) {
		return ResearchProgression.giveRecursiveResearch(player, key);
	}

	@Override
	public boolean revokeRecursiveResearch(ServerPlayer player, Identifier key) {
		return ResearchProgression.revokeRecursiveResearch(player, key);
	}

	@Override
	public boolean doesPlayerHaveRequisites(Player player, Identifier key) {
		return ResearchProgression.doesPlayerHaveRequisites(player, key);
	}

	private static Snapshot snapshot(boolean clientSide) {
		Map<Identifier, ResearchEntry> entries = RESEARCH.entries(clientSide);
		Map<Identifier, ResearchCategory> categories = CATEGORIES.entries(clientSide);
		Snapshot current = clientSide ? clientSnapshot : serverSnapshot;
		if (current.sourceEntries != entries || current.sourceCategories != categories) {
			current = build(entries, categories);
			if (clientSide) {
				clientSnapshot = current;
			} else {
				serverSnapshot = current;
			}
		}
		return current;
	}

	private static void rebuildServer() {
		Map<Identifier, ResearchEntry> entries = RESEARCH.entries(false);
		if (serverSnapshot.sourceEntries != entries) {
			reportUnresolvedItemReferences(entries);
		}
		serverSnapshot = build(entries, CATEGORIES.entries(false));
	}

	private static void reportUnresolvedItemReferences(Map<Identifier, ResearchEntry> entries) {
		int count = 0;
		LinkedHashSet<Identifier> unresolved = new LinkedHashSet<>();
		for (ResearchEntry entry : entries.values()) {
			for (var icon : entry.icons()) {
				if (icon instanceof Item item) {
					count += countUnresolved(item.item(), unresolved);
				}
			}
			for (ItemReference reward : entry.rewardItem()) {
				count += countUnresolved(reward, unresolved);
			}
			for (ResearchStage stage : entry.stages()) {
				for (ItemRequirement requirement : stage.requiredItem()) {
					count += requirement.item().map(item -> countUnresolved(item, unresolved)).orElse(0);
				}
				for (ItemRequirement requirement : stage.requiredCraft()) {
					count += requirement.item().map(item -> countUnresolved(item, unresolved)).orElse(0);
				}
			}
		}
		unresolvedItemReferenceCount = count;
		unresolved.forEach(item -> LOGGER.warn("Unresolved research item reference {}", item));
		LOGGER.info("Loaded research with {} unresolved item references", count);
	}

	private static int countUnresolved(ItemReference reference, LinkedHashSet<Identifier> unresolved) {
		if (reference.isRegistered()) {
			return 0;
		}
		unresolved.add(reference.item());
		return 1;
	}

	private static void rebuildClient() {
		clientSnapshot = build(RESEARCH.entries(true), CATEGORIES.entries(true));
	}

	private static Snapshot build(
			Map<Identifier, ResearchEntry> sourceEntries,
			Map<Identifier, ResearchCategory> sourceCategories
	) {
		LinkedHashMap<Identifier, ResearchCategory> orderedCategories = new LinkedHashMap<>();
		sourceCategories.entrySet().stream()
				.sorted(Comparator.<Map.Entry<Identifier, ResearchCategory>>comparingInt(entry -> entry.getValue().sortOrder())
						.thenComparing(Map.Entry::getKey))
				.forEach(entry -> orderedCategories.put(entry.getKey(), entry.getValue()));

		LinkedHashMap<Identifier, ResearchEntry> orderedEntries = new LinkedHashMap<>();
		sourceEntries.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> {
					Identifier key = entry.getKey();
					ResearchEntry value = entry.getValue();
					if (!orderedCategories.containsKey(value.category())) {
						LOGGER.error("Skipping research {}: category {} is not loaded", key, value.category());
						return;
					}
					orderedEntries.put(key, value);
				});

		for (Map.Entry<Identifier, ResearchEntry> entry : orderedEntries.entrySet()) {
			for (var parent : entry.getValue().parents()) {
				if (!sourceEntries.containsKey(parent.key())) {
					LOGGER.warn("Research {} references unknown parent {}", entry.getKey(), parent.key());
				}
			}
		}

		LinkedHashMap<Identifier, List<Identifier>> entriesByCategory = new LinkedHashMap<>();
		LinkedHashMap<Identifier, MutableBounds> mutableBounds = new LinkedHashMap<>();
		for (Map.Entry<Identifier, ResearchCategory> category : orderedCategories.entrySet()) {
			entriesByCategory.put(category.getKey(), new ArrayList<>());
		}
		for (Map.Entry<Identifier, ResearchEntry> entry : orderedEntries.entrySet()) {
			Identifier category = entry.getValue().category();
			entriesByCategory.get(category).add(entry.getKey());
			entry.getValue().location().ifPresent(location ->
					mutableBounds.computeIfAbsent(category, ignored -> new MutableBounds()).include(location.column(), location.row())
			);
		}
		LinkedHashMap<Identifier, ResearchBounds> bounds = new LinkedHashMap<>();
		mutableBounds.forEach((category, value) -> bounds.put(category, value.toBounds()));
		LinkedHashMap<Identifier, List<Identifier>> immutableByCategory = new LinkedHashMap<>();
		entriesByCategory.forEach((category, entries) -> immutableByCategory.put(category, List.copyOf(entries)));
		return new Snapshot(
				sourceEntries,
				sourceCategories,
				Collections.unmodifiableMap(orderedEntries),
				Collections.unmodifiableMap(orderedCategories),
				List.copyOf(orderedCategories.keySet()),
				Collections.unmodifiableMap(immutableByCategory),
				Collections.unmodifiableMap(bounds)
		);
	}

	private static final class MutableBounds {
		private int minColumn = Integer.MAX_VALUE;
		private int maxColumn = Integer.MIN_VALUE;
		private int minRow = Integer.MAX_VALUE;
		private int maxRow = Integer.MIN_VALUE;

		private void include(int column, int row) {
			minColumn = Math.min(minColumn, column);
			maxColumn = Math.max(maxColumn, column);
			minRow = Math.min(minRow, row);
			maxRow = Math.max(maxRow, row);
		}

		private ResearchBounds toBounds() {
			return new ResearchBounds(minColumn, maxColumn, minRow, maxRow);
		}
	}

	private record Snapshot(
			Map<Identifier, ResearchEntry> sourceEntries,
			Map<Identifier, ResearchCategory> sourceCategories,
			Map<Identifier, ResearchEntry> entries,
			Map<Identifier, ResearchCategory> categories,
			List<Identifier> categoryOrder,
			Map<Identifier, List<Identifier>> entriesByCategory,
			Map<Identifier, ResearchBounds> bounds
	) {
		private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of());
	}
}
