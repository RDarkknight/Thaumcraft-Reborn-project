package io.github.rdarkknight.thaumcraftreborn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public final class DebugSparkParticle extends SingleQuadParticle {
	public DebugSparkParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
		super(level, x, y, z, sprites.first());
		quadSize = 0.12F;
		lifetime = 18;
		gravity = 0.0F;
		setColor(0.45F, 0.9F, 1.0F);
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}
}
