package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ResearchStatus implements StringRepresentable {
	UNKNOWN,
	COMPLETE,
	IN_PROGRESS;

	public static final Codec<ResearchStatus> CODEC = StringRepresentable.fromEnum(ResearchStatus::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
