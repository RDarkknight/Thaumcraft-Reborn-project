package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import com.mojang.serialization.MapCodec;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;

final class SalisMundusRecipeSerializer {
	private static final MapCodec<SalisMundusRecipe> CODEC = MapCodec.unit(SalisMundusRecipe::new);
	static final RecipeSerializer<SalisMundusRecipe> SERIALIZER = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			ThaumcraftReborn.id("salis_mundus"),
			new RecipeSerializer<>(
					CODEC,
					ByteBufCodecs.fromCodecWithRegistries(CODEC.codec())
			)
	);

	private SalisMundusRecipeSerializer() {
	}

	static void init() {
	}
}
