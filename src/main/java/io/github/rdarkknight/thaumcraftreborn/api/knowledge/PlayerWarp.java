package io.github.rdarkknight.thaumcraftreborn.api.knowledge;

import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;

public interface PlayerWarp {
	PlayerWarp EMPTY = new PlayerWarp() {
		@Override
		public int get(WarpType type) {
			return 0;
		}

		@Override
		public int getCounter() {
			return 0;
		}
	};

	int get(WarpType type);

	int getCounter();
}
