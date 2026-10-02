package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProbePongPayload(int nonce, int interactions) implements CustomPacketPayload {
	public static final Type<ProbePongPayload> TYPE = new Type<>(ThaumcraftReborn.id("probe_pong"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ProbePongPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ProbePongPayload::nonce,
			ByteBufCodecs.VAR_INT, ProbePongPayload::interactions,
			ProbePongPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
