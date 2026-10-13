package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ProgressResearchPayload(Identifier research) implements CustomPacketPayload {
	public static final Type<ProgressResearchPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("progress_research"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ProgressResearchPayload> STREAM_CODEC =
			StreamCodec.composite(Identifier.STREAM_CODEC, ProgressResearchPayload::research, ProgressResearchPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
