package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenThaumonomiconScreenPayload() implements CustomPacketPayload {
	public static final OpenThaumonomiconScreenPayload INSTANCE = new OpenThaumonomiconScreenPayload();
	public static final Type<OpenThaumonomiconScreenPayload> TYPE =
			new Type<>(ThaumcraftRebornApi.id("open_thaumonomicon_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenThaumonomiconScreenPayload> STREAM_CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
