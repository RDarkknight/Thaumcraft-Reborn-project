package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ResearchAddendum(String text, List<Identifier> recipes, List<ResearchRequirement> requiredResearch) {
	public static final Codec<ResearchAddendum> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("text").forGetter(ResearchAddendum::text),
			Identifier.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchAddendum::recipes),
			ResearchRequirement.CODEC.listOf().optionalFieldOf("required_research", List.of()).forGetter(ResearchAddendum::requiredResearch)
	).apply(instance, ResearchAddendum::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchAddendum> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchAddendum {
		recipes = List.copyOf(recipes);
		requiredResearch = List.copyOf(requiredResearch);
	}
}
