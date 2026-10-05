package io.github.rdarkknight.thaumcraftreborn.core.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PlayerKnowledgeData(Map<Identifier, ResearchState> research, Map<KnowledgeKey, Integer> knowledge) {
	private static final Codec<List<ResearchFlag>> RESEARCH_FLAGS_CODEC = ResearchFlag.CODEC.listOf();
	private static final Codec<Set<ResearchFlag>> RESEARCH_FLAGS_SET_CODEC = RESEARCH_FLAGS_CODEC.xmap(
			flags -> flags.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(flags)),
			flags -> flags.stream().sorted().toList()
	);
	private static final Codec<Map<Identifier, ResearchState>> RESEARCH_CODEC =
			Codec.unboundedMap(Identifier.CODEC, ResearchState.CODEC);
	private static final Codec<List<KnowledgeValue>> KNOWLEDGE_VALUES_CODEC = KnowledgeValue.CODEC.listOf();
	private static final Codec<Map<KnowledgeKey, Integer>> KNOWLEDGE_CODEC = KNOWLEDGE_VALUES_CODEC.xmap(
			values -> {
				Map<KnowledgeKey, Integer> result = new LinkedHashMap<>();
				values.forEach(value -> result.put(value.key(), value.amount()));
				return Collections.unmodifiableMap(result);
			},
			values -> values.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.map(entry -> new KnowledgeValue(entry.getKey(), entry.getValue()))
					.toList()
	);

	public static final PlayerKnowledgeData EMPTY = new PlayerKnowledgeData(Map.of(), Map.of());
	public static final Codec<PlayerKnowledgeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESEARCH_CODEC.optionalFieldOf("research", Map.of()).forGetter(PlayerKnowledgeData::research),
			KNOWLEDGE_CODEC.optionalFieldOf("knowledge", Map.of()).forGetter(PlayerKnowledgeData::knowledge)
	).apply(instance, PlayerKnowledgeData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledgeData> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public PlayerKnowledgeData {
		research = immutableMap(research);
		knowledge = immutableMap(knowledge);
	}

	public PlayerKnowledgeData withResearch(Identifier key, ResearchState state) {
		Map<Identifier, ResearchState> next = new LinkedHashMap<>(research);
		if (state.isEmpty()) {
			next.remove(key);
		} else {
			next.put(key, state);
		}
		return new PlayerKnowledgeData(next, knowledge);
	}

	public PlayerKnowledgeData withKnowledge(KnowledgeKey key, int amount) {
		Map<KnowledgeKey, Integer> next = new LinkedHashMap<>(knowledge);
		if (amount == 0) {
			next.remove(key);
		} else {
			next.put(key, amount);
		}
		return new PlayerKnowledgeData(research, next);
	}

	public PlayerKnowledgeData clearKnowledge() {
		return new PlayerKnowledgeData(research, Map.of());
	}

	public PlayerKnowledgeData clear() {
		return EMPTY;
	}

	private static <K, V> Map<K, V> immutableMap(Map<K, V> values) {
		return Collections.unmodifiableMap(new LinkedHashMap<>(values));
	}

	public record ResearchState(boolean known, int stage, Set<ResearchFlag> flags) {
		public static final ResearchState EMPTY = new ResearchState(false, 0, Set.of());
		public static final Codec<ResearchState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("known", true).forGetter(ResearchState::known),
				Codec.INT.optionalFieldOf("stage", 0).forGetter(ResearchState::stage),
				RESEARCH_FLAGS_SET_CODEC.optionalFieldOf("flags", Set.of()).forGetter(ResearchState::flags)
		).apply(instance, ResearchState::new));

		public ResearchState {
			flags = flags.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(flags));
		}

		public ResearchState withKnown(boolean value) {
			return new ResearchState(value, stage, flags);
		}

		public ResearchState withStage(int value) {
			return new ResearchState(known, value, flags);
		}

		public ResearchState withFlag(ResearchFlag flag, boolean enabled) {
			Set<ResearchFlag> next = flags.isEmpty() ? EnumSet.noneOf(ResearchFlag.class) : EnumSet.copyOf(flags);
			if (enabled) {
				next.add(flag);
			} else {
				next.remove(flag);
			}
			return new ResearchState(known, stage, next);
		}

		public boolean isEmpty() {
			return !known && stage == 0 && flags.isEmpty();
		}
	}

	public record KnowledgeKey(KnowledgeType type, Optional<Identifier> category) implements Comparable<KnowledgeKey> {
		public static final Codec<KnowledgeKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeKey::type),
				Identifier.CODEC.optionalFieldOf("category").forGetter(KnowledgeKey::category)
		).apply(instance, KnowledgeKey::new));

		public KnowledgeKey {
			category = category == null ? Optional.empty() : category;
		}

		@Override
		public int compareTo(KnowledgeKey other) {
			int typeOrder = Integer.compare(type.ordinal(), other.type.ordinal());
			if (typeOrder != 0) {
				return typeOrder;
			}
			return category.map(Identifier::toString).orElse("")
					.compareTo(other.category.map(Identifier::toString).orElse(""));
		}
	}

	private record KnowledgeValue(KnowledgeKey key, int amount) {
		private static final Codec<KnowledgeValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				KnowledgeKey.CODEC.fieldOf("key").forGetter(KnowledgeValue::key),
				Codec.INT.fieldOf("amount").forGetter(KnowledgeValue::amount)
		).apply(instance, KnowledgeValue::new));
	}
}
