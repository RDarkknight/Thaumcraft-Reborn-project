package io.github.rdarkknight.thaumcraftreborn.systems;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectDefinitions;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectMappings;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.AuraSystems;
import io.github.rdarkknight.thaumcraftreborn.systems.aura.ChunkAuraAccess;
import io.github.rdarkknight.thaumcraftreborn.systems.knowledge.AttachedKnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchIndex;
import io.github.rdarkknight.thaumcraftreborn.systems.research.ResearchProgression;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.RecipeSystems;

public final class SystemsBootstrap {
	private SystemsBootstrap() {
	}

	public static void init() {
		AspectDefinitions.init();
		AspectLookup.install(AspectMappings.ACCESS);
		AspectMappings.init();
		AuraAccess.install(ChunkAuraAccess.INSTANCE);
		KnowledgeAccess.install(AttachedKnowledgeAccess.INSTANCE);
		ResearchIndex.init();
		ResearchProgression.init();
		RecipeSystems.init();
		CrystalGrowth.init();
		AuraSystems.init();
		ThaumcraftWorldgen.init();
		ThaumometerSystem.init();
		PickupResearchFlags.init();
	}
}
