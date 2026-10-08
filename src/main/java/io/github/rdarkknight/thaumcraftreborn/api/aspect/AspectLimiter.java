package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import java.util.Objects;

@FunctionalInterface
public interface AspectLimiter {
	AspectLimiter IDENTITY = aspects -> aspects;

	AspectList limit(AspectList aspects);

	static AspectLimiter get() {
		return Holder.INSTANCE;
	}

	static void install(AspectLimiter limiter) {
		Holder.INSTANCE = Objects.requireNonNull(limiter);
	}

	final class Holder {
		private static volatile AspectLimiter INSTANCE = IDENTITY;

		private Holder() {
		}
	}
}
