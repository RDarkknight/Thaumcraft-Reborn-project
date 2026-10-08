package io.github.rdarkknight.thaumcraftreborn.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;

public record ResearchReference(Identifier key, OptionalInt stage, boolean hiddenLine) {
	public static final Codec<ResearchReference> CODEC = Codec.STRING.comapFlatMap(
			ResearchReference::parse,
			ResearchReference::toString
	);

	public ResearchReference {
		stage = stage == null ? OptionalInt.empty() : stage;
	}

	public ResearchReference(Identifier key) {
		this(key, OptionalInt.empty(), false);
	}

	public static DataResult<ResearchReference> parse(String value) {
		boolean hiddenLine = value.startsWith("~");
		String reference = hiddenLine ? value.substring(1) : value;
		int separator = reference.lastIndexOf('@');
		OptionalInt stage = OptionalInt.empty();
		if (separator >= 0) {
			try {
				stage = OptionalInt.of(Integer.parseInt(reference.substring(separator + 1)));
			} catch (NumberFormatException exception) {
				return DataResult.error(() -> "Invalid research stage in reference: " + value);
			}
			reference = reference.substring(0, separator);
		}
		Identifier key = Identifier.tryParse(reference);
		if (key == null) {
			return DataResult.error(() -> "Invalid research key in reference: " + value);
		}
		return DataResult.success(new ResearchReference(key, stage, hiddenLine));
	}

	@Override
	public String toString() {
		return (hiddenLine ? "~" : "") + key + (stage.isPresent() ? "@" + stage.getAsInt() : "");
	}
}
