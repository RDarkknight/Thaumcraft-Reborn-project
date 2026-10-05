package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ResearchFlag implements StringRepresentable {
	PAGE,
	RESEARCH,
	POPUP;

	public static final Codec<ResearchFlag> CODEC = StringRepresentable.fromEnum(ResearchFlag::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
