package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugData;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugCommands;
import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchMenu;

public final class ContentBootstrap {
	private ContentBootstrap() {
	}

	public static void init() {
		ThaumcraftContent.init();
		ArcaneWorkbenchMenu.init();
		DebugContent.init();
		DebugContainerMenu.init();
		DebugData.init();
		DebugFx.init();
		SalisMundusFx.init();
		SalisMundusSoundEvents.init();
		DebugCommands.init();
		ResearchCommands.init();
	}
}
