package io.github.rdarkknight.thaumcraftreborn.core.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AuraChunkData(short base, float vis, float flux) {
	public static final Codec<AuraChunkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.SHORT.fieldOf("base").forGetter(AuraChunkData::base),
			Codec.FLOAT.fieldOf("vis").forGetter(AuraChunkData::vis),
			Codec.FLOAT.fieldOf("flux").forGetter(AuraChunkData::flux)
	).apply(instance, AuraChunkData::new));

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
