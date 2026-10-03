package io.github.rdarkknight.thaumcraftreborn.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface FxHandler<D> {
	void play(ClientLevel level, Vec3 origin, D data);
}
