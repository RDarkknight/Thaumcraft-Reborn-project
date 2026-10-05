package io.github.rdarkknight.thaumcraftreborn.core.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerWarpData(int permanent, int normal, int temporary, int counter) {
	public static final PlayerWarpData EMPTY = new PlayerWarpData(0, 0, 0, 0);
	public static final Codec<PlayerWarpData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("permanent", 0).forGetter(PlayerWarpData::permanent),
			Codec.INT.optionalFieldOf("normal", 0).forGetter(PlayerWarpData::normal),
			Codec.INT.optionalFieldOf("temporary", 0).forGetter(PlayerWarpData::temporary),
			Codec.INT.optionalFieldOf("counter", 0).forGetter(PlayerWarpData::counter)
	).apply(instance, PlayerWarpData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerWarpData> STREAM_CODEC =
			ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public PlayerWarpData {
		permanent = clamp(permanent);
		normal = clamp(normal);
		temporary = clamp(temporary);
	}

	public int get(WarpType type) {
		return switch (type) {
			case PERMANENT -> permanent;
			case NORMAL -> normal;
			case TEMPORARY -> temporary;
		};
	}

	public PlayerWarpData with(WarpType type, int amount) {
		return switch (type) {
			case PERMANENT -> new PlayerWarpData(amount, normal, temporary, counter);
			case NORMAL -> new PlayerWarpData(permanent, amount, temporary, counter);
			case TEMPORARY -> new PlayerWarpData(permanent, normal, amount, counter);
		};
	}

	public PlayerWarpData withCounter(int amount) {
		return new PlayerWarpData(permanent, normal, temporary, amount);
	}

	private static int clamp(int amount) {
		return Math.clamp(amount, 0, 500);
	}
}
