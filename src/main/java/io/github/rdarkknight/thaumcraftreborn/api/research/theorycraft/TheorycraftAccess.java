package io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface TheorycraftAccess {
	TheorycraftAccess NONE = new TheorycraftAccess() {
		@Override
		public void registerCard(TheorycraftCard card) {
		}

		@Override
		public void registerAid(TheorycraftAid aid) {
		}

		@Override
		public Optional<TheorycraftCard> card(Identifier id) {
			return Optional.empty();
		}

		@Override
		public Optional<TheorycraftAid> aid(Identifier id) {
			return Optional.empty();
		}

		@Override
		public Collection<TheorycraftCard> cards() {
			return java.util.List.of();
		}

		@Override
		public Collection<TheorycraftAid> aids() {
			return java.util.List.of();
		}
	};

	void registerCard(TheorycraftCard card);

	void registerAid(TheorycraftAid aid);

	Optional<TheorycraftCard> card(Identifier id);

	Optional<TheorycraftAid> aid(Identifier id);

	Collection<TheorycraftCard> cards();

	Collection<TheorycraftAid> aids();

	static TheorycraftAccess get() {
		return Holder.INSTANCE;
	}

	static void install(TheorycraftAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	final class Holder {
		private static volatile TheorycraftAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
