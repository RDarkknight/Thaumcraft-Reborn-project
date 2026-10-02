package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProbePingPayload(int nonce) implements CustomPacketPayload {
	public static final Type<ProbePingPayload> TYPE = new Type<>(ThaumcraftReborn.id("probe_ping"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ProbePingPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ProbePingPayload::nonce,
			ProbePingPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
