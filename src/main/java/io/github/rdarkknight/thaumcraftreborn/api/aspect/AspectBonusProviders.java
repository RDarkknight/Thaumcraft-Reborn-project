package io.github.rdarkknight.thaumcraftreborn.api.aspect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public final class AspectBonusProviders {
	private static final Map<Identifier, AspectBonusProvider> PROVIDERS = new LinkedHashMap<>();
	private static volatile List<AspectBonusProvider> snapshot = List.of();

	private AspectBonusProviders() {
	}

	public static synchronized void register(Identifier id, AspectBonusProvider provider) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(provider);
		if (PROVIDERS.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate aspect bonus provider id: " + id);
		}
		PROVIDERS.put(id, provider);
		snapshot = List.copyOf(PROVIDERS.values());
	}

	public static List<AspectBonusProvider> providers() {
		return snapshot;
	}
}
