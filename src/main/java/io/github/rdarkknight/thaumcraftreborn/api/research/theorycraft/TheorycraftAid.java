package io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public interface TheorycraftAid {
	Identifier id();

	boolean matches(Level level, BlockPos pos);

	List<Identifier> cards();
}
