package io.github.rdarkknight.thaumcraftreborn.systems.aspect;

import com.mojang.logging.LogUtils;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.core.data.SyncedDataLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public final class AspectMappings {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<CacheKey, AspectList> ITEM_CACHE = new ConcurrentHashMap<>();
	private static final Set<Item> WARNED_MULTIPLE_TAGS = ConcurrentHashMap.newKeySet();
	public static final SyncedDataLoader<AspectMappingFile> LOADER = new SyncedDataLoader<>(
			ThaumcraftRebornApi.id("aspect_mappings"),
			AspectMappingFile.CODEC,
			net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(AspectMappingFile.CODEC)
	);
	public static final AspectLookup ACCESS = new MappingLookup();

	private static volatile Snapshot serverSnapshot = Snapshot.empty();
	private static volatile Snapshot clientSnapshot = Snapshot.empty();

	static {
		LOADER.addServerEntriesListener(AspectMappings::invalidate);
		LOADER.addClientEntriesListener(AspectMappings::invalidate);
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, clientSide) -> invalidate());
	}

	private AspectMappings() {
	}

	public static void init() {
	}

	public static int unknownServerRegistryTargetCount() {
		return snapshot(false).unknownRegistryTargetCount();
	}

	private static AspectList lookupItem(Level level, ItemStack stack) {
		if (stack.isEmpty()) {
			return AspectList.EMPTY;
		}
		boolean clientSide = level.isClientSide();
		Item item = stack.getItem();
		CacheKey cacheKey = new CacheKey(clientSide, item);
		AspectList cached = ITEM_CACHE.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		Snapshot snapshot = snapshot(clientSide);
		Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
		AspectList result = snapshot.items().get(itemId);
		if (result == null) {
			List<Map.Entry<Identifier, AspectList>> matches = snapshot.tags().entrySet().stream()
					.filter(entry -> stack.is(TagKey.create(Registries.ITEM, entry.getKey())))
					.sorted(Map.Entry.comparingByKey())
					.toList();
			if (!matches.isEmpty()) {
				if (matches.size() > 1 && WARNED_MULTIPLE_TAGS.add(item)) {
					LOGGER.warn(
							"Item {} matches multiple aspect tags {}; using {}",
							itemId,
							matches.stream().map(Map.Entry::getKey).toList(),
							matches.getFirst().getKey()
					);
				}
				result = matches.getFirst().getValue();
			}
		}

		AspectList resolved = result == null ? AspectList.EMPTY : result;
		ITEM_CACHE.put(cacheKey, resolved);
		return resolved;
	}

	private static AspectList lookupEntity(Entity entity) {
		Snapshot snapshot = snapshot(entity.level().isClientSide());
		Identifier entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		List<EntityRule> rules = snapshot.entities().get(entityType);
		if (rules == null) {
			return AspectList.EMPTY;
		}
		for (EntityRule rule : rules) {
			if (rule.nbt().isPresent() && rule.nbt().get().matches(entity)) {
				return rule.aspects();
			}
		}
		for (EntityRule rule : rules) {
			if (rule.nbt().isEmpty()) {
				return rule.aspects();
			}
		}
		return AspectList.EMPTY;
	}

	private static Snapshot snapshot(boolean clientSide) {
		Map<Identifier, AspectMappingFile> entries = LOADER.entries(clientSide);
		Snapshot current = clientSide ? clientSnapshot : serverSnapshot;
		if (current.source() == entries) {
			return current;
		}
		synchronized (AspectMappings.class) {
			current = clientSide ? clientSnapshot : serverSnapshot;
			if (current.source() != entries) {
				current = Snapshot.create(entries, !clientSide);
				if (clientSide) {
					clientSnapshot = current;
				} else {
					serverSnapshot = current;
				}
				invalidate();
			}
		}
		return current;
	}

	private static void invalidate() {
		ITEM_CACHE.clear();
		WARNED_MULTIPLE_TAGS.clear();
	}

	private static final class MappingLookup implements AspectLookup {
		@Override
		public AspectList getAspects(Level level, ItemStack stack) {
			return lookupItem(level, stack);
		}

		@Override
		public AspectList getAspects(Entity entity) {
			return lookupEntity(entity);
		}
	}

	private record CacheKey(boolean clientSide, Item item) {
	}

	private record Target(TargetType type, Identifier id, Optional<net.minecraft.advancements.predicates.NbtPredicate> nbt) {
		@Override
		public String toString() {
			return type.name().toLowerCase() + ":" + id + nbt.map(value -> " " + value.tag()).orElse("");
		}
	}

	private record RegistryTarget(TargetType type, Identifier id) {
	}

	private record EntityRule(Optional<net.minecraft.advancements.predicates.NbtPredicate> nbt, AspectList aspects) {
	}

	private enum TargetType {
		ITEM,
		TAG,
		ENTITY
	}

	private record Snapshot(
			Map<Identifier, AspectMappingFile> source,
			Map<Identifier, AspectList> items,
			Map<Identifier, AspectList> tags,
			Map<Identifier, List<EntityRule>> entities,
			int unknownRegistryTargetCount
	) {
		private static Snapshot empty() {
			return new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), 0);
		}

		private static Snapshot create(Map<Identifier, AspectMappingFile> source, boolean validateRegistryTargets) {
			Map<Target, AspectList> merged = new LinkedHashMap<>();
			List<Map.Entry<Identifier, AspectMappingFile>> orderedFiles = source.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.toList();
			for (Map.Entry<Identifier, AspectMappingFile> file : orderedFiles) {
				for (AspectMappingFile.Entry entry : file.getValue().entries()) {
					if (entry.replace()) {
						continue;
					}
					Target target = target(entry);
					AspectList previous = merged.get(target);
					if (previous == null) {
						merged.put(target, entry.aspects());
					} else {
						LOGGER.debug("Adding aspect mapping from {} to {}", file.getKey(), target);
						merged.put(target, previous.add(entry.aspects()));
					}
				}
			}

			Map<Target, Integer> replacements = new LinkedHashMap<>();
			Set<Target> duplicateReplacementWarnings = new LinkedHashSet<>();
			for (Map.Entry<Identifier, AspectMappingFile> file : orderedFiles) {
				for (AspectMappingFile.Entry entry : file.getValue().entries()) {
					if (entry.replace()) {
						Target target = target(entry);
						int count = replacements.merge(target, 1, Integer::sum);
						LOGGER.warn("Replacing aspect mapping from {} for {}", file.getKey(), target);
						if (count > 1 && duplicateReplacementWarnings.add(target)) {
							LOGGER.warn("Multiple replacement aspect mappings for {}; the last one wins", target);
						}
						merged.put(target, entry.aspects());
					}
				}
			}

			Map<Identifier, AspectList> items = new LinkedHashMap<>();
			Map<Identifier, AspectList> tags = new LinkedHashMap<>();
			Map<Identifier, List<EntityRule>> entities = new LinkedHashMap<>();
			Set<RegistryTarget> unknownTargets = new LinkedHashSet<>();
			merged.forEach((target, aspects) -> {
				switch (target.type()) {
					case ITEM -> {
						items.put(target.id(), aspects);
						if (validateRegistryTargets && !BuiltInRegistries.ITEM.containsKey(target.id())) {
							unknownTargets.add(new RegistryTarget(target.type(), target.id()));
						}
					}
					case TAG -> tags.put(target.id(), aspects);
					case ENTITY -> {
						entities.computeIfAbsent(target.id(), ignored -> new ArrayList<>())
								.add(new EntityRule(target.nbt(), aspects));
						if (validateRegistryTargets && !BuiltInRegistries.ENTITY_TYPE.containsKey(target.id())) {
							unknownTargets.add(new RegistryTarget(target.type(), target.id()));
						}
					}
				}
			});
			unknownTargets.forEach(target -> LOGGER.warn("Unknown {} registry ID in aspect mappings: {}",
					target.type().name().toLowerCase(), target.id()));
			entities.replaceAll((id, rules) -> List.copyOf(rules));
			return new Snapshot(
					source,
					Map.copyOf(items),
					Map.copyOf(tags),
					Map.copyOf(entities),
					unknownTargets.size()
			);
		}

		private static Target target(AspectMappingFile.Entry entry) {
			if (entry.item().isPresent()) {
				return new Target(TargetType.ITEM, entry.item().get(), Optional.empty());
			}
			if (entry.tag().isPresent()) {
				return new Target(TargetType.TAG, entry.tag().get(), Optional.empty());
			}
			return new Target(TargetType.ENTITY, entry.entity().orElseThrow(), entry.nbt());
		}
	}
}
