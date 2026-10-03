package io.github.rdarkknight.thaumcraftreborn.client.fx;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DebugBurstHandler {
	private static final AtomicInteger EXECUTIONS = new AtomicInteger();

	private DebugBurstHandler() {
	}

	public static void play(ClientLevel level, Vec3 origin, Integer requestedCount) {
		EXECUTIONS.incrementAndGet();
		int count = Mth.clamp(requestedCount, 1, 32);
		for (int index = 0; index < count; index++) {
			double angle = Math.PI * 2.0 * index / count;
			double radius = 0.25 + (index % 4) * 0.08;
			level.addParticle(
					DebugFx.DEBUG_SPARK,
					origin.x + Math.cos(angle) * radius,
					origin.y + (index % 3) * 0.08,
					origin.z + Math.sin(angle) * radius,
					Math.cos(angle) * 0.015,
					0.025 + (index % 2) * 0.01,
					Math.sin(angle) * 0.015
			);
		}
	}

	public static int executionCount() {
		return EXECUTIONS.get();
	}

	public static void resetExecutionCount() {
		EXECUTIONS.set(0);
	}
}
