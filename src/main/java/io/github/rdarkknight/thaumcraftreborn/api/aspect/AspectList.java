package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public final class AspectList {
	private static final int MAX_STREAM_ENTRIES = 65536;
	private static final Codec<Map<Aspect, Integer>> MAP_CODEC = Codec.unboundedMap(Aspect.CODEC, ExtraCodecs.POSITIVE_INT);

	public static final AspectList EMPTY = new AspectList(Map.of());
	public static final Codec<AspectList> CODEC = MAP_CODEC.xmap(AspectList::fromMap, AspectList::asMap);
	public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC =
			StreamCodec.of(AspectList::encode, AspectList::decode);

	private final Map<Aspect, Integer> amounts;

	private AspectList(Map<Aspect, Integer> amounts) {
		this.amounts = Collections.unmodifiableMap(new LinkedHashMap<>(amounts));
	}

	public static AspectList of(Aspect aspect, int amount) {
		return amount <= 0 ? EMPTY : new AspectList(Map.of(Objects.requireNonNull(aspect), amount));
	}

	public static Builder builder() {
		return new Builder();
	}

	public int amount(Aspect aspect) {
		return amounts.getOrDefault(aspect, 0);
	}

	public List<Aspect> aspects() {
		return List.copyOf(amounts.keySet());
	}

	public int size() {
		return amounts.size();
	}

	public int visSize() {
		return amounts.values().stream().mapToInt(Integer::intValue).sum();
	}

	public boolean isEmpty() {
		return amounts.isEmpty();
	}

	public AspectList add(Aspect aspect, int amount) {
		if (amount == 0) {
			return this;
		}
		return builder().add(this).add(aspect, amount).build();
	}

	public AspectList add(AspectList other) {
		return builder().add(this).add(other).build();
	}

	public AspectList merge(Aspect aspect, int amount) {
		return builder().add(this).merge(aspect, amount).build();
	}

	public AspectList merge(AspectList other) {
		return builder().add(this).merge(other).build();
	}

	public AspectList remove(Aspect aspect, int amount) {
		if (amount <= 0) {
			return this;
		}
		return builder().add(this).remove(aspect, amount).build();
	}

	public AspectList remove(Aspect aspect) {
		return builder().add(this).remove(aspect).build();
	}

	public AspectList remove(AspectList other) {
		return builder().add(this).remove(other).build();
	}

	public Optional<AspectList> reduce(Aspect aspect, int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Reduction amount must not be negative");
		}
		if (this.amount(aspect) < amount) {
			return Optional.empty();
		}
		return Optional.of(remove(aspect, amount));
	}

	public List<Aspect> sortedByName() {
		return amounts.keySet().stream()
				.sorted((first, second) -> first.tag().compareTo(second.tag()))
				.toList();
	}

	public List<Aspect> sortedByAmount() {
		return amounts.keySet().stream()
				.sorted((first, second) -> Integer.compare(amount(second), amount(first)))
				.toList();
	}

	private static AspectList fromMap(Map<Aspect, Integer> values) {
		Builder builder = builder();
		values.forEach(builder::add);
		return builder.build();
	}

	private Map<Aspect, Integer> asMap() {
		return amounts;
	}

	private static void encode(RegistryFriendlyByteBuf buffer, AspectList list) {
		ByteBufCodecs.VAR_INT.encode(buffer, list.amounts.size());
		list.amounts.forEach((aspect, amount) -> {
			Aspect.STREAM_CODEC.encode(buffer, aspect);
			ByteBufCodecs.VAR_INT.encode(buffer, amount);
		});
	}

	private static AspectList decode(RegistryFriendlyByteBuf buffer) {
		int size = ByteBufCodecs.VAR_INT.decode(buffer);
		if (size < 0 || size > MAX_STREAM_ENTRIES) {
			throw new IllegalArgumentException("Invalid aspect list size: " + size);
		}
		Builder builder = builder();
		Set<Aspect> seen = new HashSet<>();
		for (int index = 0; index < size; index++) {
			Aspect aspect = Aspect.STREAM_CODEC.decode(buffer);
			int amount = ByteBufCodecs.VAR_INT.decode(buffer);
			if (amount <= 0) {
				throw new IllegalArgumentException("Aspect amount must be positive: " + amount);
			}
			if (!seen.add(aspect)) {
				throw new IllegalArgumentException("Duplicate aspect in stream: " + aspect.tag());
			}
			builder.add(aspect, amount);
		}
		return builder.build();
	}

	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof AspectList aspectList && amounts.equals(aspectList.amounts);
	}

	@Override
	public int hashCode() {
		return amounts.hashCode();
	}

	@Override
	public String toString() {
		return amounts.toString();
	}

	public static final class Builder {
		private final LinkedHashMap<Aspect, Integer> amounts = new LinkedHashMap<>();

		private Builder() {
		}

		public Builder add(Aspect aspect, int amount) {
			Objects.requireNonNull(aspect);
			int total = Math.addExact(amounts.getOrDefault(aspect, 0), amount);
			if (total <= 0) {
				amounts.remove(aspect);
			} else {
				amounts.put(aspect, total);
			}
			return this;
		}

		public Builder add(AspectList other) {
			other.amounts.forEach(this::add);
			return this;
		}

		public Builder merge(Aspect aspect, int amount) {
			Objects.requireNonNull(aspect);
			if (amount > amounts.getOrDefault(aspect, 0)) {
				amounts.put(aspect, amount);
			}
			return this;
		}

		public Builder merge(AspectList other) {
			other.amounts.forEach(this::merge);
			return this;
		}

		public Builder remove(Aspect aspect, int amount) {
			if (amount > 0) {
				add(aspect, -amount);
			}
			return this;
		}

		public Builder remove(Aspect aspect) {
			amounts.remove(aspect);
			return this;
		}

		public Builder remove(AspectList other) {
			other.amounts.forEach(this::remove);
			return this;
		}

		public AspectList build() {
			return amounts.isEmpty() ? EMPTY : new AspectList(amounts);
		}
	}
}
