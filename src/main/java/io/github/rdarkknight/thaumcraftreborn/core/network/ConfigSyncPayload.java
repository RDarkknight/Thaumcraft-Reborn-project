package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.core.config.CommonConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(CommonConfig config) implements CustomPacketPayload {
	public static final Type<ConfigSyncPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("config_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.fromCodecWithRegistries(CommonConfig.CODEC),
			ConfigSyncPayload::config,
			ConfigSyncPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
