package io.github.rdarkknight.thaumcraftreborn.client.particle;

import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public final class DebugParticles {
	private DebugParticles() {
	}

	public static void register() {
		ParticleProviderRegistry.getInstance().register(
				DebugFx.DEBUG_SPARK,
				sprites -> (options, level, x, y, z, xAux, yAux, zAux, random) ->
						new DebugSparkParticle(level, x, y, z, sprites)
		);
	}
}
