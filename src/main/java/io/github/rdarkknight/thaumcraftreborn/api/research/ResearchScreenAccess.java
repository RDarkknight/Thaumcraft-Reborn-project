package io.github.rdarkknight.thaumcraftreborn.api.research;

import java.util.Objects;

public final class ResearchScreenAccess {
	private static Runnable opener = () -> {
	};
	private static Runnable requester = () -> {
	};

	private ResearchScreenAccess() {
	}

	public static void install(Runnable screenOpener) {
		opener = Objects.requireNonNull(screenOpener);
	}

	public static void installRequester(Runnable screenRequester) {
		requester = Objects.requireNonNull(screenRequester);
	}

	public static void open() {
		opener.run();
	}

	public static void requestOpen() {
		requester.run();
	}
}
