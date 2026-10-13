package io.github.rdarkknight.thaumcraftreborn.core.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AuraChunkData(short base, float vis, float flux) {
	public static final Codec<AuraChunkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.SHORT.fieldOf("base").forGetter(AuraChunkData::base),
			Codec.FLOAT.fieldOf("vis").forGetter(AuraChunkData::vis),
			Codec.FLOAT.fieldOf("flux").forGetter(AuraChunkData::flux)
	).apply(instance, AuraChunkData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, AuraChunkData> STREAM_CODEC = StreamCodec.of(
			(buffer, data) -> {
				ByteBufCodecs.SHORT.encode(buffer, data.base());
				ByteBufCodecs.FLOAT.encode(buffer, data.vis());
				ByteBufCodecs.FLOAT.encode(buffer, data.flux());
			},
			buffer -> new AuraChunkData(
					ByteBufCodecs.SHORT.decode(buffer),
					ByteBufCodecs.FLOAT.decode(buffer),
					ByteBufCodecs.FLOAT.decode(buffer)
			)
	);
	public AuraChunkData {
		vis = clamp(vis);
		flux = clamp(flux);
	}

	public AuraChunkData withVis(float amount) {
		return new AuraChunkData(base, amount, flux);
	}

	public AuraChunkData withFlux(float amount) {
		return new AuraChunkData(base, vis, amount);
	}

	private static float clamp(float value) {
		if (Float.isNaN(value)) {
			return 0.0F;
		}
		return Math.min(32766.0F, Math.max(0.0F, value));
	}
}
