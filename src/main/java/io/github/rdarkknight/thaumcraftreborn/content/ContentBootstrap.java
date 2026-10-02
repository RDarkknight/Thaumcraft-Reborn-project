package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;

public final class ContentBootstrap {
	private ContentBootstrap() {
	}

	public static void init() {
		DebugContent.init();
	}
}
