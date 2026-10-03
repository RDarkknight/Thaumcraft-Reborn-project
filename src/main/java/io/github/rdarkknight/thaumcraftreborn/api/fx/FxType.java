package io.github.rdarkknight.thaumcraftreborn.api.fx;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class FxType<D> {
	private final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec;

	public FxType(StreamCodec<RegistryFriendlyByteBuf, D> streamCodec) {
		this.streamCodec = streamCodec;
	}

	public StreamCodec<RegistryFriendlyByteBuf, D> streamCodec() {
		return streamCodec;
	}
}
