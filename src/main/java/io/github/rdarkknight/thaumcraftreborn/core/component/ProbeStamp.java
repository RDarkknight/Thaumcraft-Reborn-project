package io.github.rdarkknight.thaumcraftreborn.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProbeStamp(int uses, String lastUser) {
	public static final Codec<ProbeStamp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("uses").forGetter(ProbeStamp::uses),
			Codec.STRING.fieldOf("last_user").forGetter(ProbeStamp::lastUser)
	).apply(instance, ProbeStamp::new));

	public static final StreamCodec<ByteBuf, ProbeStamp> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ProbeStamp::uses,
			ByteBufCodecs.STRING_UTF8, ProbeStamp::lastUser,
			ProbeStamp::new
	);
}
