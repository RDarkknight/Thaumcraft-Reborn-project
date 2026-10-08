package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum KnowledgeType implements StringRepresentable {
	THEORY(32, "T"),
	OBSERVATION(16, "O");

	public static final Codec<KnowledgeType> CODEC = StringRepresentable.fromEnum(KnowledgeType::values);

	private final int progression;
	private final String abbreviation;

	KnowledgeType(int progression, String abbreviation) {
		this.progression = progression;
		this.abbreviation = abbreviation;
	}

	public int progression() {
		return progression;
	}

	public String abbreviation() {
		return abbreviation;
	}

	public boolean hasFields() {
		return true;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
