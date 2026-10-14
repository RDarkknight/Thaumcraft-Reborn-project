package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class SalisMundusSoundEvents {
	public static final SoundEvent USE = Registry.register(
			BuiltInRegistries.SOUND_EVENT,
			ThaumcraftRebornApi.id("salis_mundus"),
			SoundEvent.createVariableRangeEvent(ThaumcraftRebornApi.id("salis_mundus"))
	);

	private SalisMundusSoundEvents() {
	}

	public static void init() {
	}
}
