package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum WarpType implements StringRepresentable {
	PERMANENT,
	NORMAL,
	TEMPORARY;

	public static final Codec<WarpType> CODEC = StringRepresentable.fromEnum(WarpType::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
