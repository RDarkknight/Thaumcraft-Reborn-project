package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ResearchStage(
		String text,
		List<Identifier> recipes,
		List<ItemRequirement> requiredItem,
		List<ItemRequirement> requiredCraft,
		List<KnowledgeAmount> requiredKnowledge,
		List<ResearchRequirement> requiredResearch,
		int warp
) {
	public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("text").forGetter(ResearchStage::text),
			Identifier.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchStage::recipes),
			ItemRequirement.CODEC.listOf().optionalFieldOf("required_item", List.of()).forGetter(ResearchStage::requiredItem),
			ItemRequirement.CODEC.listOf().optionalFieldOf("required_craft", List.of()).forGetter(ResearchStage::requiredCraft),
			KnowledgeAmount.CODEC.listOf().optionalFieldOf("required_knowledge", List.of()).forGetter(ResearchStage::requiredKnowledge),
			ResearchRequirement.CODEC.listOf().optionalFieldOf("required_research", List.of()).forGetter(ResearchStage::requiredResearch),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("warp", 0).forGetter(ResearchStage::warp)
	).apply(instance, ResearchStage::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchStage> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchStage {
		recipes = List.copyOf(recipes);
		requiredItem = List.copyOf(requiredItem);
		requiredCraft = List.copyOf(requiredCraft);
		requiredKnowledge = List.copyOf(requiredKnowledge);
		requiredResearch = List.copyOf(requiredResearch);
	}
}
