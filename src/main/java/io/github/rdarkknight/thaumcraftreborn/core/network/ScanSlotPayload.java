package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ScanSlotPayload(int containerId, int slotId) implements CustomPacketPayload {
	public static final Type<ScanSlotPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("scan_slot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ScanSlotPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			ScanSlotPayload::containerId,
			ByteBufCodecs.VAR_INT,
			ScanSlotPayload::slotId,
			ScanSlotPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
