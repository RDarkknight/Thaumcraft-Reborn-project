package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

interface AuraSimulation {
	void tick(ServerLevel level, LevelChunk chunk);
}
