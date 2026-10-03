package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugData;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugCommands;

public final class ContentBootstrap {
	private ContentBootstrap() {
	}

	public static void init() {
		DebugContent.init();
		DebugContainerMenu.init();
		DebugData.init();
		DebugFx.init();
		DebugCommands.init();
	}
}
