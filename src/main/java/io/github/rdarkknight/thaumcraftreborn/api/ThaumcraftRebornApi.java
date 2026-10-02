package io.github.rdarkknight.thaumcraftreborn.api;

import net.minecraft.resources.Identifier;

public final class ThaumcraftRebornApi {
	public static final String MOD_ID = "thaumcraft_reborn";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private ThaumcraftRebornApi() {
	}
}
