package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ResearchMeta implements StringRepresentable {
	ROUND,
	SPIKY,
	REVERSE,
	HIDDEN,
	AUTOUNLOCK,
	HEX;

	public static final Codec<ResearchMeta> CODEC = StringRepresentable.fromEnum(ResearchMeta::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
