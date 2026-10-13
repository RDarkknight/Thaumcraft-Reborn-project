package io.github.rdarkknight.thaumcraftreborn.api.dust;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class DustTriggerRegistry {
	private static final Map<Identifier, DustTrigger> TRIGGERS = new LinkedHashMap<>();

	private DustTriggerRegistry() {
	}

	public static synchronized void register(Identifier id, DustTrigger trigger) {
		if (TRIGGERS.putIfAbsent(id, trigger) != null) {
			throw new IllegalArgumentException("Duplicate dust trigger id: " + id);
		}
	}

	public static synchronized List<DustTrigger> triggers() {
		return List.copyOf(new ArrayList<>(TRIGGERS.values()));
	}
}
