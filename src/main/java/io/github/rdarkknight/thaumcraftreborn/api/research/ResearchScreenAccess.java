package io.github.rdarkknight.thaumcraftreborn.api.research;

import java.util.Objects;

public final class ResearchScreenAccess {
	private static Runnable opener = () -> {
	};

	private ResearchScreenAccess() {
	}

	public static void install(Runnable screenOpener) {
		opener = Objects.requireNonNull(screenOpener);
	}

	public static void open() {
		opener.run();
	}
}
