package io.github.rdarkknight.thaumcraftreborn.content.debug;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DebugDataEntry(String label, int value) {
	public static final Codec<DebugDataEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("label").forGetter(DebugDataEntry::label),
			Codec.INT.fieldOf("value").forGetter(DebugDataEntry::value)
	).apply(instance, DebugDataEntry::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, DebugDataEntry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			DebugDataEntry::label,
			ByteBufCodecs.VAR_INT,
			DebugDataEntry::value,
			DebugDataEntry::new
	);
}
