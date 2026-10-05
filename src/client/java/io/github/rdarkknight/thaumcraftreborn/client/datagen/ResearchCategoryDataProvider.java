package io.github.rdarkknight.thaumcraftreborn.client.datagen;

import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchCategory;
import io.github.rdarkknight.thaumcraftreborn.systems.aspect.AspectDefinitions;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

final class ResearchCategoryDataProvider implements DataProvider {
	private static final List<Definition> DEFINITIONS = List.of(
			new Definition("basics", category(
					"basics", 0, null, formula(amount(AspectDefinitions.PLANT, 5), amount(AspectDefinitions.ORDER, 5),
							amount(AspectDefinitions.ENTROPY, 5), amount(AspectDefinitions.AIR, 5), amount(AspectDefinitions.FIRE, 5),
							amount(AspectDefinitions.EARTH, 3), amount(AspectDefinitions.WATER, 5))
			)),
			new Definition("auromancy", category(
					"auromancy", 1, "unlockauromancy", formula(amount(AspectDefinitions.AURA, 20), amount(AspectDefinitions.MAGIC, 20),
							amount(AspectDefinitions.FLUX, 15), amount(AspectDefinitions.CRYSTAL, 5), amount(AspectDefinitions.COLD, 5),
							amount(AspectDefinitions.AIR, 5))
			)),
			new Definition("alchemy", category(
					"alchemy", 2, "unlockalchemy", formula(amount(AspectDefinitions.ALCHEMY, 30), amount(AspectDefinitions.FLUX, 10),
							amount(AspectDefinitions.MAGIC, 10), amount(AspectDefinitions.LIFE, 5), amount(AspectDefinitions.AVERSION, 5),
							amount(AspectDefinitions.DESIRE, 5), amount(AspectDefinitions.WATER, 5))
			)),
			new Definition("artifice", category(
					"artifice", 3, "unlockartifice", formula(amount(AspectDefinitions.MECHANISM, 10), amount(AspectDefinitions.CRAFT, 10),
							amount(AspectDefinitions.METAL, 10), amount(AspectDefinitions.TOOL, 10), amount(AspectDefinitions.ENERGY, 10),
							amount(AspectDefinitions.LIGHT, 5), amount(AspectDefinitions.FLIGHT, 5), amount(AspectDefinitions.TRAP, 5),
							amount(AspectDefinitions.FIRE, 5))
			)),
			new Definition("infusion", category(
					"infusion", 4, "unlockinfusion", formula(amount(AspectDefinitions.MAGIC, 30), amount(AspectDefinitions.PROTECT, 10),
							amount(AspectDefinitions.TOOL, 10), amount(AspectDefinitions.FLUX, 5), amount(AspectDefinitions.CRAFT, 5),
							amount(AspectDefinitions.SOUL, 5), amount(AspectDefinitions.EARTH, 3))
			)),
			new Definition("golemancy", category(
					"golemancy", 5, "unlockgolemancy", formula(amount(AspectDefinitions.MAN, 20), amount(AspectDefinitions.MOTION, 10),
							amount(AspectDefinitions.MIND, 10), amount(AspectDefinitions.MECHANISM, 10), amount(AspectDefinitions.EXCHANGE, 5),
							amount(AspectDefinitions.SENSES, 5), amount(AspectDefinitions.BEAST, 5), amount(AspectDefinitions.ORDER, 5))
			)),
			new Definition("eldritch", category(
					"eldritch", 6, "unlockeldritch", formula(amount(AspectDefinitions.ELDRITCH, 20), amount(AspectDefinitions.DARKNESS, 10),
							amount(AspectDefinitions.MAGIC, 5), amount(AspectDefinitions.MIND, 5), amount(AspectDefinitions.VOID, 5),
							amount(AspectDefinitions.DEATH, 5), amount(AspectDefinitions.UNDEAD, 5), amount(AspectDefinitions.ENTROPY, 5))
			))
	);

	private final PackOutput.PathProvider paths;

	ResearchCategoryDataProvider(FabricPackOutput output) {
		this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "thaumcraft_reborn/research_categories");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		CompletableFuture<?>[] writes = DEFINITIONS.stream()
				.map(definition -> DataProvider.saveStable(
						cachedOutput,
						ResearchCategory.CODEC.encodeStart(JsonOps.INSTANCE, definition.category()).getOrThrow(),
						paths.json(ThaumcraftRebornApi.id(definition.name()))
				))
				.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(writes);
	}

	@Override
	public String getName() {
		return "Thaumcraft Reborn Research Categories";
	}

	private static ResearchCategory category(String id, int order, String researchKey, AspectList formula) {
		return new ResearchCategory(
				order,
				researchKey == null ? Optional.empty() : Optional.of(ThaumcraftRebornApi.id(researchKey)),
				formula,
				ThaumcraftRebornApi.id("textures/research/category_" + id),
				ThaumcraftRebornApi.id("textures/research/background_" + id),
				Optional.of(ThaumcraftRebornApi.id("textures/research/background_overlay"))
		);
	}

	private static AspectList formula(AspectAmount... aspectAmounts) {
		AspectList.Builder builder = AspectList.builder();
		for (AspectAmount aspectAmount : aspectAmounts) {
			builder.add(aspectAmount.aspect(), aspectAmount.amount());
		}
		return builder.build();
	}

	private static AspectAmount amount(Aspect aspect, int amount) {
		return new AspectAmount(aspect, amount);
	}

	private record AspectAmount(Aspect aspect, int amount) {
	}

	private record Definition(String name, ResearchCategory category) {
	}
}
