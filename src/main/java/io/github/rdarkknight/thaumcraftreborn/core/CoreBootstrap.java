package io.github.rdarkknight.thaumcraftreborn.core;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.network.ModPayloads;

public final class CoreBootstrap {
	private CoreBootstrap() {
	}

	public static void init() {
		ModDataComponents.init();
		ModAttachments.init();
		ModPayloads.init();
		ThaumcraftConfig.init();
	}
}
