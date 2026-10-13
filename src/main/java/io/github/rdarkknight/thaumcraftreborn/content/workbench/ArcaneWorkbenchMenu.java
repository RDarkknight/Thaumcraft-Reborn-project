package io.github.rdarkknight.thaumcraftreborn.content.workbench;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.RecipeGateAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.menu.ThaumcraftMenu;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;
import io.github.rdarkknight.thaumcraftreborn.systems.RevealingEquipment;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ArcaneWorkbenchMenu extends ThaumcraftMenu {
	public static final ExtendedMenuType<ArcaneWorkbenchMenu, BlockPos> TYPE = Registry.register(
			BuiltInRegistries.MENU,
			ThaumcraftReborn.id("arcane_workbench"),
			new ExtendedMenuType<>(ArcaneWorkbenchMenu::new, BlockPos.STREAM_CODEC)
	);

	private static final List<net.minecraft.resources.ResourceKey<Aspect>> PRIMAL_ASPECTS =
			List.of(Aspects.AIR, Aspects.EARTH, Aspects.FIRE, Aspects.WATER, Aspects.ORDER, Aspects.ENTROPY);
	private final Inventory playerInventory;
	private final BlockPos pos;
	private final SimpleContainer output = new SimpleContainer(1);
	private Match currentMatch;
	private RecipeManager cachedRecipeManager;
	private List<ThaumcraftRecipe> cachedArcaneRecipes = List.of();

	public static void init() {
	}

	public ArcaneWorkbenchMenu(int containerId, Inventory inventory, BlockPos pos) {
		super(TYPE, containerId, createMachine(inventory, pos));
		this.playerInventory = inventory;
		this.pos = pos;
		for (int slot = 0; slot < 9; slot++) {
			addSlot(new InputSlot(machine(), slot, 17 + slot % 3 * 18, 18 + slot / 3 * 18, this));
		}
		for (int slot = 0; slot < 6; slot++) {
			addSlot(new CrystalSlot(machine(), slot + 9, 91 + slot % 3 * 18, 18 + slot / 3 * 18, slot, this));
		}
		addSlot(new OutputSlot(output, 0, 178, 36, this));
		addPlayerInventory(inventory, 8, 118);
		recomputeOutput();
	}

	private static Container createMachine(Inventory inventory, BlockPos pos) {
		if (inventory.player.level().getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity blockEntity) {
			return blockEntity;
		}
		return new SimpleContainer(15);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index == 15) {
			return quickMoveResult(player);
		}
		return super.quickMoveStack(player, index);
	}

	private ItemStack quickMoveResult(Player player) {
		ItemStack result = output.getItem(0);
		if (result.isEmpty() || !canFitEntireResult(player, result) || !canTakeResult(player, result)) {
			return ItemStack.EMPTY;
		}
		ItemStack original = result.copy();
		int[] inventoryCounts = new int[36];
		for (int slot = 0; slot < inventoryCounts.length; slot++) {
			inventoryCounts[slot] = player.getInventory().getItem(slot).getCount();
		}
		ItemStack moving = original.copy();
		if (!moveItemStackTo(moving, 16, slots.size(), true) || !moving.isEmpty()) {
			rollbackInventoryTransfer(player, original, inventoryCounts);
			return ItemStack.EMPTY;
		}
		output.setItem(0, ItemStack.EMPTY);
		output.setChanged();
		if (!takeResult(player, original)) {
			rollbackInventoryTransfer(player, original, inventoryCounts);
			output.setItem(0, original.copy());
			output.setChanged();
			return ItemStack.EMPTY;
		}
		return original;
	}

	private boolean canFitEntireResult(Player player, ItemStack result) {
		int remaining = result.getCount();
		for (int slot = 0; slot < 36; slot++) {
			ItemStack existing = player.getInventory().getItem(slot);
			if (existing.isEmpty()) {
				remaining -= result.getMaxStackSize();
			} else if (ItemStack.isSameItemSameComponents(existing, result)) {
				remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
			}
			if (remaining <= 0) {
				return true;
			}
		}
		return false;
	}

	private void rollbackInventoryTransfer(Player player, ItemStack result, int[] countsBefore) {
		for (int slot = 0; slot < countsBefore.length; slot++) {
			ItemStack current = player.getInventory().getItem(slot);
			if (ItemStack.isSameItemSameComponents(current, result)) {
				int added = current.getCount() - countsBefore[slot];
				if (added > 0) {
					current.shrink(added);
				}
			}
		}
		player.getInventory().setChanged();
	}

	private void recomputeOutput() {
		Level level = playerInventory.player.level();
		if (level.isClientSide()) {
			return;
		}
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		currentMatch = findMatch(serverLevel, playerInventory.player);
		output.setItem(0, currentMatch == null || !hasPayment(playerInventory.player, currentMatch)
				? ItemStack.EMPTY : currentMatch.result().copy());
	}

	private Match findMatch(ServerLevel level, Player player) {
		CraftingInput input = craftingInput();
		ThaumcraftRecipe arcane = arcaneRecipes(level).stream()
				.filter(recipe -> recipe.matches(input, level))
				.filter(recipe -> recipe.gate().map(gate -> RecipeGateAccess.get().isUnlocked(player, gate)).orElse(true))
				.findFirst().orElse(null);
		if (arcane != null) {
			return new Match(arcane, null, arcane.assemble(input));
		}
		Optional<RecipeHolder<CraftingRecipe>> vanilla =
				level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
		return vanilla.map(holder -> new Match(null, holder, holder.value().assemble(input))).orElse(null);
	}

	private List<ThaumcraftRecipe> arcaneRecipes(ServerLevel level) {
		RecipeManager recipeManager = level.getServer().getRecipeManager();
		if (recipeManager != cachedRecipeManager) {
			cachedRecipeManager = recipeManager;
			cachedArcaneRecipes = recipeManager.getRecipes().stream()
					.map(RecipeHolder::value)
					.filter(ThaumcraftRecipe.class::isInstance)
					.map(ThaumcraftRecipe.class::cast)
					.filter(recipe -> recipe.getType() == ThaumcraftRecipe.arcaneShapedType()
							|| recipe.getType() == ThaumcraftRecipe.arcaneShapelessType())
					.toList();
		}
		return cachedArcaneRecipes;
	}

	private CraftingInput craftingInput() {
		List<ItemStack> inputs = new ArrayList<>(9);
		for (int slot = 0; slot < 9; slot++) {
			inputs.add(machine().getItem(slot));
		}
		return CraftingInput.of(3, 3, inputs);
	}

	private boolean hasCrystals(ThaumcraftRecipe recipe) {
		if (recipe.crystalComponents().size() == 6) {
			for (int slot = 0; slot < 6; slot++) {
				ItemStack stack = machine().getItem(slot + 9);
				Aspect required = ThaumcraftRegistries.ASPECT.getValue(PRIMAL_ASPECTS.get(slot).identifier());
				if (stack.getCount() < 1 || !stack.is(ThaumcraftContent.CRYSTAL_ESSENCE)
						|| required == null || !required.equals(stack.get(ModDataComponents.CRYSTAL_ASPECT))
						|| !recipe.crystalComponents().get(slot).test(stack)) {
					return false;
				}
			}
			return true;
		}
		int[] remaining = new int[6];
		for (int slot = 0; slot < 6; slot++) {
			remaining[slot] = machine().getItem(slot + 9).getCount();
		}
		for (var ingredient : recipe.crystalComponents()) {
			boolean found = false;
			for (int slot = 0; slot < 6; slot++) {
				ItemStack stack = machine().getItem(slot + 9);
				if (remaining[slot] > 0 && ingredient.test(stack)) {
					remaining[slot]--;
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	private int visCost(Player player, ThaumcraftRecipe recipe) {
		float discount = RevealingEquipment.isRevealing(player) ? 0.05F : 0.0F;
		return (int) (recipe.visCost() * (1.0F - discount));
	}

	private boolean takeResult(Player player, ItemStack delivered) {
		Match match = takeableMatch(player, delivered);
		if (match == null || !(player.level() instanceof ServerLevel serverLevel)) {
			recomputeOutput();
			return false;
		}
		if (match.arcane() != null) {
			int cost = visCost(player, match.arcane());
			float drained = AuraAccess.get().drainVis(serverLevel, pos, cost, false);
			if (drained < cost) {
				AuraAccess.get().addVis(serverLevel, pos, drained);
				recomputeOutput();
				return false;
			}
			consumeCrystals(match.arcane());
		}
		ItemStack crafted = match.result().copy();
		consumeIngredients(player, match);
		if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			CraftEventRecorder.record(serverPlayer, crafted);
		}
		recomputeOutput();
		return true;
	}

	private Match takeableMatch(Player player, ItemStack expectedResult) {
		if (!(player.level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		Match match = findMatch(serverLevel, player);
		if (match == null || !ItemStack.isSameItemSameComponents(expectedResult, match.result())) {
			return null;
		}
		if (!hasPayment(player, match)) {
			return null;
		}
		return match;
	}

	private boolean hasPayment(Player player, Match match) {
		if (match.arcane() == null) {
			return true;
		}
		if (!(player.level() instanceof ServerLevel serverLevel)) {
			return false;
		}
		int cost = visCost(player, match.arcane());
		return hasCrystals(match.arcane())
				&& AuraAccess.get().drainVis(serverLevel, pos, cost, true) >= cost;
	}

	private boolean canTakeResult(Player player, ItemStack expectedResult) {
		return takeableMatch(player, expectedResult) != null;
	}

	private void restoreDelivered(Player player, ItemStack delivered) {
		var menu = player.containerMenu;
		ItemStack carried = menu.getCarried();
		if (carried.isEmpty()) {
			menu.setCarried(delivered.copy());
		} else if (ItemStack.isSameItemSameComponents(carried, delivered)) {
			ItemStack restored = carried.copy();
			restored.shrink(Math.min(delivered.getCount(), restored.getCount()));
			menu.setCarried(restored);
			if (carried.getCount() < delivered.getCount()) {
				placeBack(player, delivered.copyWithCount(delivered.getCount() - carried.getCount()));
			}
		} else {
			placeBack(player, delivered.copy());
		}
	}

	private void placeBack(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false, Prediction.SERVER_ONLY);
		}
	}

	private void consumeCrystals(ThaumcraftRecipe recipe) {
		if (recipe.crystalComponents().size() == 6) {
			for (int slot = 0; slot < 6; slot++) {
				machine().getItem(slot + 9).shrink(1);
			}
			machine().setChanged();
			return;
		}
		for (var ingredient : recipe.crystalComponents()) {
			for (int slot = 0; slot < 6; slot++) {
				ItemStack stack = machine().getItem(slot + 9);
				if (ingredient.test(stack) && !stack.isEmpty()) {
					stack.shrink(1);
					machine().setChanged();
					break;
				}
			}
		}
	}

	private void consumeIngredients(Player player, Match match) {
		CraftingInput input = craftingInput();
		List<ItemStack> remaining = match.vanilla() == null
				? java.util.Collections.nCopies(9, ItemStack.EMPTY)
				: match.vanilla().value().getRemainingItems(input);
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = machine().getItem(slot);
			if (!stack.isEmpty()) {
				stack.shrink(1);
				machine().setChanged();
			}
			ItemStack remainder = remaining.get(slot);
			if (!remainder.isEmpty()) {
				if (machine().getItem(slot).isEmpty()) {
					machine().setItem(slot, remainder.copy());
				} else {
					ItemStack dropped = remainder.copy();
					if (!dropped.isEmpty()) {
						if (!player.getInventory().add(dropped)) {
							player.drop(dropped, false, Prediction.SERVER_ONLY);
						}
					}
				}
			}
		}
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		recomputeOutput();
	}

	private record Match(ThaumcraftRecipe arcane, RecipeHolder<CraftingRecipe> vanilla, ItemStack result) {
	}

	private static final class InputSlot extends Slot {
		private final ArcaneWorkbenchMenu menu;

		private InputSlot(Container container, int index, int x, int y, ArcaneWorkbenchMenu menu) {
			super(container, index, x, y);
			this.menu = menu;
		}

		@Override
		public void setChanged() {
			super.setChanged();
			menu.recomputeOutput();
		}
	}

	private static final class CrystalSlot extends Slot {
		private final int aspectSlot;
		private final ArcaneWorkbenchMenu menu;

		private CrystalSlot(Container container, int index, int x, int y, int aspectSlot, ArcaneWorkbenchMenu menu) {
			super(container, index, x, y);
			this.aspectSlot = aspectSlot;
			this.menu = menu;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			if (!stack.is(ThaumcraftContent.CRYSTAL_ESSENCE)) {
				return false;
			}
			Aspect required = ThaumcraftRegistries.ASPECT.getValue(PRIMAL_ASPECTS.get(aspectSlot).identifier());
			return required != null && required.equals(stack.get(ModDataComponents.CRYSTAL_ASPECT));
		}

		@Override
		public void setChanged() {
			super.setChanged();
			menu.recomputeOutput();
		}
	}

	private static final class OutputSlot extends Slot {
		private final ArcaneWorkbenchMenu menu;

		private OutputSlot(Container container, int index, int x, int y, ArcaneWorkbenchMenu menu) {
			super(container, index, x, y);
			this.menu = menu;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(Player player) {
			return hasItem() && (player.level().isClientSide() || menu.canTakeResult(player, getItem()));
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			if (!menu.takeResult(player, stack)) {
				menu.restoreDelivered(player, stack);
			}
		}
	}
}
