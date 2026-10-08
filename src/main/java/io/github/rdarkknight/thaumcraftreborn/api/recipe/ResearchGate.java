package io.github.rdarkknight.thaumcraftreborn.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import java.util.Locale;
import java.util.OptionalInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public record ResearchGate(Identifier key, OptionalInt stage) {
	public static final Codec<ResearchGate> CODEC = Codec.STRING.comapFlatMap(
			ResearchGate::parse,
			ResearchGate::toString
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchGate> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public ResearchGate {
		stage = stage == null ? OptionalInt.empty() : stage;
	}

	public static DataResult<ResearchGate> parse(String value) {
		int separator = value.lastIndexOf('@');
		OptionalInt stage = OptionalInt.empty();
		String rawKey = value;
		if (separator >= 0) {
			try {
				stage = OptionalInt.of(Integer.parseInt(value.substring(separator + 1)));
			} catch (NumberFormatException exception) {
				return DataResult.error(() -> "Invalid research stage in gate: " + value);
			}
			if (stage.getAsInt() < 0) {
				return DataResult.error(() -> "Research stage must be non-negative: " + value);
			}
			rawKey = value.substring(0, separator);
		}
		String normalized = rawKey.toLowerCase(Locale.ROOT);
		Identifier key = normalized.indexOf(':') < 0
				? ThaumcraftRebornApi.id(normalized)
				: Identifier.tryParse(normalized);
		if (key == null) {
			return DataResult.error(() -> "Invalid research key in gate: " + value);
		}
		return DataResult.success(new ResearchGate(key, stage));
	}

	@Override
	public String toString() {
		return key + (stage.isPresent() ? "@" + stage.getAsInt() : "");
	}
}
