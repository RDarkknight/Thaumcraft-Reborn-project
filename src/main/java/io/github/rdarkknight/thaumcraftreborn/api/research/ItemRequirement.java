package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemRequirement(Optional<ItemStackTemplate> item, Optional<Identifier> tag, int count) {
	private static final Codec<ItemRequirement> ITEM_CODEC = ItemStackTemplate.CODEC.fieldOf("item").codec()
			.xmap(ItemRequirement::forItem, requirement -> requirement.item().orElseThrow());
	private static final Codec<ItemRequirement> TAG_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("tag").forGetter(requirement -> requirement.tag().orElseThrow()),
			ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(ItemRequirement::count)
	).apply(instance, ItemRequirement::forTag));
	public static final Codec<ItemRequirement> CODEC = Codec.either(ITEM_CODEC, TAG_CODEC).xmap(
			value -> value.map(requirement -> requirement, requirement -> requirement),
			requirement -> requirement.item().isPresent()
					? Either.left(requirement)
					: Either.right(requirement)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemRequirement> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ItemRequirement {
		item = item == null ? Optional.empty() : item;
		tag = tag == null ? Optional.empty() : tag;
		if (item.isPresent() == tag.isPresent()) {
			throw new IllegalArgumentException("Item requirement must have exactly one of item or tag");
		}
		if (count < 1) {
			throw new IllegalArgumentException("Item requirement count must be positive");
		}
	}

	public static ItemRequirement forItem(ItemStackTemplate item) {
		return new ItemRequirement(Optional.of(item), Optional.empty(), Math.max(1, item.count()));
	}

	public static ItemRequirement forTag(Identifier tag, int count) {
		return new ItemRequirement(Optional.empty(), Optional.of(tag), count);
	}
}
