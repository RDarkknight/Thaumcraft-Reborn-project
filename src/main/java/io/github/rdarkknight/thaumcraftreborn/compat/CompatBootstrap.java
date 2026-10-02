package io.github.rdarkknight.thaumcraftreborn.compat;

import io.github.rdarkknight.thaumcraftreborn.compat.trinkets.TrinketsAccessoryAccess;
import io.github.rdarkknight.thaumcraftreborn.core.accessory.AccessoryAccess;
import net.fabricmc.loader.api.FabricLoader;

public final class CompatBootstrap {
	private CompatBootstrap() {
	}

	public static void init() {
		if (FabricLoader.getInstance().isModLoaded("trinkets_updated")) {
			AccessoryAccess.install(new TrinketsAccessoryAccess());
		}
	}
}
