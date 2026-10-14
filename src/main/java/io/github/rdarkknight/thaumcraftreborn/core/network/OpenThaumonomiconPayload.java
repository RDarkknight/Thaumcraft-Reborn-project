package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenThaumonomiconPayload() implements CustomPacketPayload {
	public static final OpenThaumonomiconPayload INSTANCE = new OpenThaumonomiconPayload();
	public static final Type<OpenThaumonomiconPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("open_thaumonomicon"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenThaumonomiconPayload> STREAM_CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
