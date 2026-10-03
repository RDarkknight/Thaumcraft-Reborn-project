package io.github.rdarkknight.thaumcraftreborn.core.fx;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import io.github.rdarkknight.thaumcraftreborn.api.registry.ThaumcraftRegistryKeys;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record FxPayload(FxType<?> fxType, Object data, Vec3 origin) implements CustomPacketPayload {
	public static final Type<FxPayload> TYPE = new Type<>(ThaumcraftRebornApi.id("fx"));
	private static final StreamCodec<RegistryFriendlyByteBuf, FxType<?>> TYPE_CODEC =
			ByteBufCodecs.registry(ThaumcraftRegistryKeys.FX_TYPE);
	public static final StreamCodec<RegistryFriendlyByteBuf, FxPayload> STREAM_CODEC =
			StreamCodec.of(FxPayload::encode, FxPayload::decode);

	public static <D> FxPayload of(FxType<D> type, D data, Vec3 origin) {
		return new FxPayload(type, data, origin);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private static void encode(RegistryFriendlyByteBuf buffer, FxPayload payload) {
		TYPE_CODEC.encode(buffer, payload.fxType);
		encodeData(buffer, payload.fxType, payload.data);
		Vec3.STREAM_CODEC.encode(buffer, payload.origin);
	}

	private static FxPayload decode(RegistryFriendlyByteBuf buffer) {
		FxType<?> fxType = TYPE_CODEC.decode(buffer);
		Object data = decodeData(buffer, fxType);
		Vec3 origin = Vec3.STREAM_CODEC.decode(buffer);
		return new FxPayload(fxType, data, origin);
	}

	@SuppressWarnings("unchecked")
	private static <D> void encodeData(RegistryFriendlyByteBuf buffer, FxType<?> type, Object data) {
		((FxType<D>) type).streamCodec().encode(buffer, (D) data);
	}

	@SuppressWarnings("unchecked")
	private static <D> Object decodeData(RegistryFriendlyByteBuf buffer, FxType<?> type) {
		return ((FxType<D>) type).streamCodec().decode(buffer);
	}
}
