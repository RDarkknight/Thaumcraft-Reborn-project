package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AuraSyncPayload(int base, float vis, float flux) implements CustomPacketPayload {
	public static final Type<AuraSyncPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("aura_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AuraSyncPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			AuraSyncPayload::base,
			ByteBufCodecs.FLOAT,
			AuraSyncPayload::vis,
			ByteBufCodecs.FLOAT,
			AuraSyncPayload::flux,
			AuraSyncPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
