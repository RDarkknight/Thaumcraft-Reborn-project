package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import com.mojang.serialization.Codec;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class Aspect {
	public static final Codec<Aspect> CODEC = Codec.lazyInitialized(() -> registry().byNameCodec());
	public static final StreamCodec<RegistryFriendlyByteBuf, Aspect> STREAM_CODEC =
			ByteBufCodecs.registry(ThaumcraftRegistryKeys.ASPECT);

	private final String tag;
	private final int color;
	private final List<Aspect> components;
	private final AspectBlend blend;
	private final Optional<ChatFormatting> chatFormatting;

	public Aspect(String tag, int color, List<Aspect> components, AspectBlend blend, Optional<ChatFormatting> chatFormatting) {
		this.tag = Objects.requireNonNull(tag);
		this.color = color;
		this.components = List.copyOf(components);
		this.blend = Objects.requireNonNull(blend);
		this.chatFormatting = Objects.requireNonNull(chatFormatting);
		if (this.components.size() != 0 && this.components.size() != 2) {
			throw new IllegalArgumentException("An aspect must be primal or have exactly two components");
		}
	}

	public String tag() {
		return tag;
	}

	public int color() {
		return color;
	}

	public List<Aspect> components() {
		return components;
	}

	public AspectBlend blend() {
		return blend;
	}

	public Optional<ChatFormatting> chatFormatting() {
		return chatFormatting;
	}

	public boolean isPrimal() {
		return components.isEmpty();
	}

	public String translationKey() {
		return "aspect.thaumcraft_reborn." + tag;
	}

	public static Registry<Aspect> registry() {
		return registryValue();
	}

	@SuppressWarnings("unchecked")
	private static Registry<Aspect> registryValue() {
		Identifier registryId = ThaumcraftRegistryKeys.ASPECT.identifier();
		Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
		if (registry == null) {
			throw new IllegalStateException("Aspect registry is not available: " + registryId);
		}
		return (Registry<Aspect>) registry;
	}
}
