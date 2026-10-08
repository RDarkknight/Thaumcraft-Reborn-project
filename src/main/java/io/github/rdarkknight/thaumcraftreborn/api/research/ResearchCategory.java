package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ResearchCategory(
		int sortOrder,
		Optional<Identifier> researchKey,
		AspectList formula,
		Identifier icon,
		Identifier background,
		Optional<Identifier> background2
) {
	public static final Codec<ResearchCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("sort_order").forGetter(ResearchCategory::sortOrder),
			Identifier.CODEC.optionalFieldOf("research_key").forGetter(ResearchCategory::researchKey),
			AspectList.CODEC.fieldOf("formula").forGetter(ResearchCategory::formula),
			Identifier.CODEC.fieldOf("icon").forGetter(ResearchCategory::icon),
			Identifier.CODEC.fieldOf("background").forGetter(ResearchCategory::background),
			Identifier.CODEC.optionalFieldOf("background2").forGetter(ResearchCategory::background2)
	).apply(instance, ResearchCategory::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchCategory> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchCategory {
		researchKey = researchKey == null ? Optional.empty() : researchKey;
		background2 = background2 == null ? Optional.empty() : background2;
	}
}
