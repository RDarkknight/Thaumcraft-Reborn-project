package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import java.util.List;
import java.util.Optional;

public final class AspectCombinations {
	private AspectCombinations() {
	}

	public static Optional<Aspect> combine(Aspect first, Aspect second) {
		for (Aspect aspect : Aspect.registry()) {
			List<Aspect> components = aspect.components();
			if (components.size() == 2
					&& (components.get(0) == first && components.get(1) == second
					|| components.get(0) == second && components.get(1) == first)) {
				return Optional.of(aspect);
			}
		}
		return Optional.empty();
	}
}
