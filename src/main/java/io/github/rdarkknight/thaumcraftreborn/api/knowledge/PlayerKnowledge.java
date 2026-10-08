package io.github.rdarkknight.thaumcraftreborn.api.knowledge;

import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchReference;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

public interface PlayerKnowledge {
	PlayerKnowledge EMPTY = new PlayerKnowledge() {
		@Override
		public ResearchStatus getResearchStatus(Identifier key) {
			return ResearchStatus.UNKNOWN;
		}

		@Override
		public boolean isResearchKnown(Identifier key) {
			return false;
		}

		@Override
		public boolean isResearchKnown(ResearchReference reference) {
			return false;
		}

		@Override
		public boolean isResearchComplete(Identifier key) {
			return false;
		}

		@Override
		public int getResearchStage(Identifier key) {
			return -1;
		}

		@Override
		public Set<Identifier> getResearchList() {
			return Set.of();
		}

		@Override
		public boolean hasResearchFlag(Identifier key, ResearchFlag flag) {
			return false;
		}

		@Override
		public int getKnowledge(KnowledgeType type, Optional<Identifier> category) {
			return 0;
		}

		@Override
		public int getKnowledgeRaw(KnowledgeType type, Optional<Identifier> category) {
			return 0;
		}
	};

	ResearchStatus getResearchStatus(Identifier key);

	boolean isResearchKnown(Identifier key);

	boolean isResearchKnown(ResearchReference reference);

	boolean isResearchComplete(Identifier key);

	int getResearchStage(Identifier key);

	Set<Identifier> getResearchList();

	boolean hasResearchFlag(Identifier key, ResearchFlag flag);

	int getKnowledge(KnowledgeType type, Optional<Identifier> category);

	int getKnowledgeRaw(KnowledgeType type, Optional<Identifier> category);
}
