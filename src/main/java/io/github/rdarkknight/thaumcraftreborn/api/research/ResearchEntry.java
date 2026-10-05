package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record ResearchEntry(
		String name,
		Identifier category,
		List<ResearchIcon> icons,
		List<ResearchReference> parents,
		List<ResearchReference> siblings,
		java.util.Optional<ResearchLocation> location,
		Set<ResearchMeta> meta,
		List<ItemStackTemplate> rewardItem,
		List<KnowledgeAmount> rewardKnowledge,
		List<ResearchStage> stages,
		List<ResearchAddendum> addenda
) {
	private static final Codec<Set<ResearchMeta>> META_CODEC = ResearchMeta.CODEC.listOf().xmap(
			values -> values.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(values)),
			values -> values.stream().sorted().toList()
	);
	private static final Codec<List<ResearchStage>> NON_EMPTY_STAGES_CODEC = ResearchStage.CODEC.listOf().validate(
			stages -> stages.isEmpty()
					? DataResult.error(() -> "Research entries must contain at least one stage")
					: DataResult.success(stages)
	);
	public static final Codec<ResearchEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(ResearchEntry::name),
			Identifier.CODEC.fieldOf("category").forGetter(ResearchEntry::category),
			ResearchIcon.CODEC.listOf().optionalFieldOf("icons", List.of()).forGetter(ResearchEntry::icons),
			ResearchReference.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(ResearchEntry::parents),
			ResearchReference.CODEC.listOf().optionalFieldOf("siblings", List.of()).forGetter(ResearchEntry::siblings),
			ResearchLocation.CODEC.optionalFieldOf("location").forGetter(ResearchEntry::location),
			META_CODEC.optionalFieldOf("meta", Set.of()).forGetter(ResearchEntry::meta),
			ItemStackTemplate.CODEC.listOf().optionalFieldOf("reward_item", List.of()).forGetter(ResearchEntry::rewardItem),
			KnowledgeAmount.CODEC.listOf().optionalFieldOf("reward_knowledge", List.of()).forGetter(ResearchEntry::rewardKnowledge),
			NON_EMPTY_STAGES_CODEC.fieldOf("stages").forGetter(ResearchEntry::stages),
			ResearchAddendum.CODEC.listOf().optionalFieldOf("addenda", List.of()).forGetter(ResearchEntry::addenda)
	).apply(instance, ResearchEntry::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchEntry> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchEntry {
		icons = List.copyOf(icons);
		parents = List.copyOf(parents);
		siblings = List.copyOf(siblings);
		location = location == null ? java.util.Optional.empty() : location;
		meta = meta.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(meta));
		rewardItem = List.copyOf(rewardItem);
		rewardKnowledge = List.copyOf(rewardKnowledge);
		stages = List.copyOf(stages);
		addenda = List.copyOf(addenda);
		if (stages.isEmpty()) {
			throw new IllegalArgumentException("Research entries must contain at least one stage");
		}
	}
}
