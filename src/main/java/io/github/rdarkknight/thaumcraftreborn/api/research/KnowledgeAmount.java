package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record KnowledgeAmount(KnowledgeType type, Optional<Identifier> category, int amount) {
	public static final Codec<KnowledgeAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeAmount::type),
			Identifier.CODEC.optionalFieldOf("category").forGetter(KnowledgeAmount::category),
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("amount").forGetter(KnowledgeAmount::amount)
	).apply(instance, KnowledgeAmount::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeAmount> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public KnowledgeAmount {
		category = category == null ? Optional.empty() : category;
	}
}
