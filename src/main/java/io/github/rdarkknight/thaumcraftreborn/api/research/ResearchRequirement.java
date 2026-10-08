package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ResearchRequirement(ResearchReference reference, Optional<Identifier> icon) {
	public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResearchReference.CODEC.fieldOf("reference").forGetter(ResearchRequirement::reference),
			Identifier.CODEC.optionalFieldOf("icon").forGetter(ResearchRequirement::icon)
	).apply(instance, ResearchRequirement::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchRequirement> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchRequirement {
		icon = icon == null ? Optional.empty() : icon;
	}
}
