package io.github.rdarkknight.thaumcraftreborn.systems;

import com.mojang.serialization.MapCodec;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.content.PrimalCrystalBlock;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class ThaumcraftWorldgen {
	private static final Feature AMBER_ORE = registerFeature("amber_ore", new Feature() {
		@Override
		public MapCodec<? extends Feature> codec() {
			return MapCodec.unit(this);
		}

		@Override
		public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - random.nextInt(25);
			BlockPos pos = new BlockPos(origin.getX(), y, origin.getZ());
			if (!level.getBlockState(pos).is(BlockTags.STONE_ORE_REPLACEABLES)) {
				return false;
			}
			setBlock(level, pos, ThaumcraftContent.ORE_AMBER.defaultBlockState());
			return true;
		}
	});

	private static final Feature PRIMAL_CRYSTALS = registerFeature("primal_crystals", new Feature() {
		@Override
		public MapCodec<? extends Feature> codec() {
			return MapCodec.unit(this);
		}

		@Override
		public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
			int chunkX = origin.getX() >> 4;
			int chunkZ = origin.getZ() >> 4;
			int density = 0;
			int placed = 0;
			for (int attempt = 0; attempt < 8; attempt++) {
				int x = chunkX * 16 + 8 + random.nextInt(13) - 6;
				int z = chunkZ * 16 + 8 + random.nextInt(13) - 6;
				int maxY = Math.max(5, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 5);
				BlockPos center = new BlockPos(x, random.nextInt(maxY), z);
				Block crystal = chooseCrystal(level, center, random);
				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (random.nextInt(3) == 0) {
								continue;
							}
							BlockPos pos = center.offset(dx, dy, dz);
							if (canPlaceCrystal(level, pos)) {
								int size = 1 + random.nextInt(3);
								setBlock(level, pos, crystal.defaultBlockState()
										.setValue(PrimalCrystalBlock.SIZE, size));
								density += size;
								placed++;
							}
						}
					}
				}
				if (density > 64) {
					break;
				}
			}
			return placed > 0;
		}
	});

	private static final List<BiomeAspect> BIOME_ASPECTS = List.of(
			new BiomeAspect(ConventionalBiomeTags.IS_AQUATIC, ThaumcraftContent.CRYSTAL_AQUA),
			new BiomeAspect(ConventionalBiomeTags.IS_OCEAN, ThaumcraftContent.CRYSTAL_AQUA),
			new BiomeAspect(ConventionalBiomeTags.IS_RIVER, ThaumcraftContent.CRYSTAL_AQUA),
			new BiomeAspect(ConventionalBiomeTags.IS_WET, ThaumcraftContent.CRYSTAL_AQUA),
			new BiomeAspect(ConventionalBiomeTags.IS_LUSH, ThaumcraftContent.CRYSTAL_AQUA),
			new BiomeAspect(ConventionalBiomeTags.IS_HOT, ThaumcraftContent.CRYSTAL_IGNIS),
			new BiomeAspect(ConventionalBiomeTags.IS_NETHER, ThaumcraftContent.CRYSTAL_IGNIS),
			new BiomeAspect(ConventionalBiomeTags.IS_BADLANDS, ThaumcraftContent.CRYSTAL_IGNIS),
			new BiomeAspect(ConventionalBiomeTags.IS_SPOOKY, ThaumcraftContent.CRYSTAL_IGNIS),
			new BiomeAspect(ConventionalBiomeTags.IS_VEGETATION_DENSE, ThaumcraftContent.CRYSTAL_ORDO),
			new BiomeAspect(ConventionalBiomeTags.IS_SNOWY, ThaumcraftContent.CRYSTAL_ORDO),
			new BiomeAspect(ConventionalBiomeTags.IS_COLD, ThaumcraftContent.CRYSTAL_ORDO),
			new BiomeAspect(ConventionalBiomeTags.IS_MUSHROOM, ThaumcraftContent.CRYSTAL_ORDO),
			new BiomeAspect(ConventionalBiomeTags.IS_MAGICAL, ThaumcraftContent.CRYSTAL_ORDO),
			new BiomeAspect(ConventionalBiomeTags.IS_CONIFEROUS_TREE, ThaumcraftContent.CRYSTAL_TERRA),
			new BiomeAspect(ConventionalBiomeTags.IS_FOREST, ThaumcraftContent.CRYSTAL_TERRA),
			new BiomeAspect(ConventionalBiomeTags.IS_SANDY, ThaumcraftContent.CRYSTAL_TERRA),
			new BiomeAspect(ConventionalBiomeTags.IS_BEACH, ThaumcraftContent.CRYSTAL_TERRA),
			new BiomeAspect(ConventionalBiomeTags.IS_JUNGLE, ThaumcraftContent.CRYSTAL_TERRA),
			new BiomeAspect(ConventionalBiomeTags.IS_SAVANNA, ThaumcraftContent.CRYSTAL_AER),
			new BiomeAspect(ConventionalBiomeTags.IS_MOUNTAIN, ThaumcraftContent.CRYSTAL_AER),
			new BiomeAspect(ConventionalBiomeTags.IS_HILL, ThaumcraftContent.CRYSTAL_AER),
			new BiomeAspect(ConventionalBiomeTags.IS_PLAINS, ThaumcraftContent.CRYSTAL_AER),
			new BiomeAspect(ConventionalBiomeTags.IS_END, ThaumcraftContent.CRYSTAL_AER),
			new BiomeAspect(ConventionalBiomeTags.IS_VEGETATION_SPARSE, ThaumcraftContent.CRYSTAL_PERDITIO),
			new BiomeAspect(ConventionalBiomeTags.IS_DRY, ThaumcraftContent.CRYSTAL_PERDITIO),
			new BiomeAspect(ConventionalBiomeTags.IS_SWAMP, ThaumcraftContent.CRYSTAL_PERDITIO),
			new BiomeAspect(ConventionalBiomeTags.IS_WASTELAND, ThaumcraftContent.CRYSTAL_PERDITIO),
			new BiomeAspect(ConventionalBiomeTags.IS_DEAD, ThaumcraftContent.CRYSTAL_PERDITIO)
	);

	private ThaumcraftWorldgen() {
	}

	public static void init() {
		addFeature("ore_amber");
		addFeature("ore_cinnabar");
		addFeature("ore_quartz");
		addFeature("primal_crystals");
	}

	private static void addFeature(String path) {
		ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> key = ResourceKey.create(
				Registries.PLACED_FEATURE,
				ThaumcraftReborn.id(path)
		);
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES,
				key
		);
	}

	private static Feature registerFeature(String path, Feature feature) {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ThaumcraftReborn.id(path), feature.codec());
		return feature;
	}

	private static boolean canPlaceCrystal(WorldGenLevel level, BlockPos pos) {
		if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced()) {
			return false;
		}
		for (Direction direction : Direction.values()) {
			BlockPos supportPos = pos.relative(direction);
			var support = level.getBlockState(supportPos);
			if (support.is(BlockTags.BASE_STONE_OVERWORLD)
					&& Block.isFaceFull(support.getCollisionShape(level, supportPos), direction.getOpposite())) {
				return true;
			}
		}
		return false;
	}

	private static Block chooseCrystal(WorldGenLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(3) == 0) {
			List<Block> matches = new ArrayList<>();
			for (BiomeAspect mapping : BIOME_ASPECTS) {
				if (level.getBiome(pos).is(mapping.tag())) {
					matches.add(mapping.crystal());
				}
			}
			if (!matches.isEmpty()) {
				return matches.get(random.nextInt(matches.size()));
			}
		}
		return List.of(
				ThaumcraftContent.CRYSTAL_AER,
				ThaumcraftContent.CRYSTAL_TERRA,
				ThaumcraftContent.CRYSTAL_IGNIS,
				ThaumcraftContent.CRYSTAL_AQUA,
				ThaumcraftContent.CRYSTAL_ORDO,
				ThaumcraftContent.CRYSTAL_PERDITIO
		).get(random.nextInt(6));
	}

	private static void setBlock(WorldGenLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
		level.setBlock(pos, state, 2);
	}

	private record BiomeAspect(TagKey<Biome> tag, Block crystal) {
	}
}
