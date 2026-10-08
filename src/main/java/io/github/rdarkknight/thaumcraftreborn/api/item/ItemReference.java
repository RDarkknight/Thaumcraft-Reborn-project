package io.github.rdarkknight.thaumcraftreborn.api.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemReference(Identifier item, int count, DataComponentPatch components) {
	private static final Codec<ItemReference> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(ItemReference::item),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ItemReference::count),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemReference::components)
	).apply(instance, ItemReference::new));
	public static final Codec<ItemReference> CODEC = Codec.either(Identifier.CODEC, OBJECT_CODEC).xmap(
			value -> value.map(item -> new ItemReference(item, 1, DataComponentPatch.EMPTY), reference -> reference),
			reference -> Either.right(reference)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemReference> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ItemReference {
		if (count < 1) {
			throw new IllegalArgumentException("Item reference count must be positive");
		}
	}

	public boolean isRegistered() {
		return BuiltInRegistries.ITEM.containsKey(item);
	}

	public Optional<ItemStack> resolve() {
		return BuiltInRegistries.ITEM.get(item)
				.map(holder -> new ItemStack(holder, count, components));
	}
}
