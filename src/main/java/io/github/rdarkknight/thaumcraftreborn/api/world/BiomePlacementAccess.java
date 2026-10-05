package io.github.rdarkknight.thaumcraftreborn.api.world;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.slf4j.Logger;

public interface BiomePlacementAccess {
	BiomePlacementAccess NONE = new Empty();

	String providerId();

	boolean isAvailable();

	void addOverworldBiome(ResourceKey<Biome> biome, Climate.ParameterPoint parameters);

	default List<Request> requests() {
		return List.of();
	}

	static BiomePlacementAccess get() {
		return Holder.INSTANCE;
	}

	static void install(BiomePlacementAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	record Request(ResourceKey<Biome> biome, Climate.ParameterPoint parameters) {
	}

	final class Empty implements BiomePlacementAccess {
		private static final Logger LOGGER = LogUtils.getLogger();
		private final List<Request> requests = new ArrayList<>();
		private final List<Request> requestView = Collections.unmodifiableList(requests);

		private Empty() {
		}

		@Override
		public String providerId() {
			return "none";
		}

		@Override
		public boolean isAvailable() {
			return false;
		}

		@Override
		public void addOverworldBiome(ResourceKey<Biome> biome, Climate.ParameterPoint parameters) {
			requests.add(new Request(biome, parameters));
			LOGGER.warn("no biome placement provider; {} not placed", biome.identifier());
		}

		@Override
		public List<Request> requests() {
			return requestView;
		}
	}

	final class Holder {
		private static volatile BiomePlacementAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
