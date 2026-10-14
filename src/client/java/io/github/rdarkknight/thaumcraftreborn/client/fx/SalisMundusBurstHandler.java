package io.github.rdarkknight.thaumcraftreborn.client.fx;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public final class SalisMundusBurstHandler {
	private SalisMundusBurstHandler() {
	}

	public static void play(ClientLevel level, Vec3 origin, Integer requestedCount) {
		int count = Math.max(0, Math.min(requestedCount, 50));
		for (int index = 0; index < count; index++) {
			double angle = Math.PI * 2.0 * index / Math.max(1, count);
			double radius = 0.2 + (index % 5) * 0.09;
			level.addParticle(
					DebugFx.DEBUG_SPARK,
					origin.x + Math.cos(angle) * radius,
					origin.y + (index % 4) * 0.07,
					origin.z + Math.sin(angle) * radius,
					Math.cos(angle) * 0.02,
					0.025 + (index % 2) * 0.01,
					Math.sin(angle) * 0.02
			);
		}
	}
}
