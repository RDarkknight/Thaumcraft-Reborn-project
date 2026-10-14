package io.github.rdarkknight.thaumcraftreborn.systems.recipe;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class SalisMundusRecipe implements CraftingRecipe {
	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() * input.height() < 6) {
			return false;
		}
		boolean bowl = false;
		boolean flint = false;
		boolean redstone = false;
		Set<Aspect> aspects = new HashSet<>();
		int used = 0;
		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}
			used++;
			if (stack.is(Items.BOWL) && !bowl) {
				bowl = true;
			} else if (stack.is(Items.FLINT) && !flint) {
				flint = true;
			} else if (stack.is(Items.REDSTONE) && !redstone) {
				redstone = true;
			} else if (stack.is(ThaumcraftContent.CRYSTAL_ESSENCE)) {
				Aspect aspect = stack.get(ModDataComponents.CRYSTAL_ASPECT);
				if (aspect == null || !aspects.add(aspect)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return used == 6 && bowl && flint && redstone && aspects.size() == 3;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return new ItemStack(ThaumcraftContent.SALIS_MUNDUS);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int index = 0; index < input.size(); index++) {
			ItemStack stack = input.getItem(index);
			if (stack.is(Items.BOWL) || stack.is(Items.FLINT)) {
				remaining.set(index, stack.copyWithCount(1));
			}
		}
		return remaining;
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return SalisMundusRecipeSerializer.SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return new RecipeBookCategory();
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public boolean showNotification() {
		return false;
	}
}
