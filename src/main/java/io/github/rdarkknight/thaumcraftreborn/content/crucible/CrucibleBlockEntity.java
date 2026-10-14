package io.github.rdarkknight.thaumcraftreborn.content.crucible;

import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspects;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.RecipeGateAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.ThaumcraftRecipe;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CrucibleBlockEntity extends ThaumcraftBlockEntity {
	public static final int CAPACITY = 1000;
	private int heat;
	private int waterMb;
	private AspectList aspects = AspectList.EMPTY;
	private long counter = -100L;
	private int damageCallbacks;

	public CrucibleBlockEntity(BlockPos pos, BlockState state) {
		super(ThaumcraftContent.CRUCIBLE_BLOCK_ENTITY, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
		if (level.isClientSide()) {
			return;
		}
		crucible.counter++;
		int previousHeat = crucible.heat;
		boolean heater = level.getBlockState(pos.below()).is(CrucibleBlock.HEATERS);
		if (crucible.waterMb > 0 && heater) {
			crucible.heat = Math.min(200, crucible.heat + 1);
		} else {
			crucible.heat = Math.max(0, crucible.heat - 1);
		}
		if (previousHeat <= 150 && crucible.heat > 150 || previousHeat > 150 && crucible.heat <= 150) {
			crucible.markDirtyAndSync();
		}
		if (crucible.aspects.visSize() > 500) {
			crucible.spillRandom();
		}
		if (crucible.counter >= 100L) {
			crucible.spillRandom();
			crucible.counter = 0L;
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		heat = Math.max(0, Math.min(200, input.read("heat", com.mojang.serialization.Codec.INT).orElse(0)));
		waterMb = Math.max(0, Math.min(CAPACITY, input.read("water", com.mojang.serialization.Codec.INT).orElse(0)));
		aspects = input.read("aspects", AspectList.CODEC).orElse(AspectList.EMPTY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("heat", com.mojang.serialization.Codec.INT, heat);
		output.store("water", com.mojang.serialization.Codec.INT, waterMb);
		output.store("aspects", AspectList.CODEC, aspects);
	}

	public int heat() {
		return heat;
	}

	public int waterMb() {
		return waterMb;
	}

	public AspectList aspects() {
		return aspects;
	}

	public void setHeat(int value) {
		heat = Math.max(0, Math.min(200, value));
		setChanged();
	}

	public void setWaterMb(int value) {
		waterMb = Math.max(0, Math.min(CAPACITY, value));
		markDirtyAndSync();
	}

	public void setAspects(AspectList value) {
		aspects = value == null ? AspectList.EMPTY : value;
		markDirtyAndSync();
	}

	public boolean isHot() {
		return heat > 150;
	}

	public ItemStack processItem(ItemStack input, ServerPlayer player) {
		if (level == null || level.isClientSide() || !isHot() || waterMb <= 0 || input.isEmpty()) {
			return input;
		}
		ItemStack remaining = input.copy();
		int stackSize = input.getCount();
		for (int index = 0; index < stackSize; index++) {
			ThaumcraftRecipe recipe = findRecipe(input, player);
			if (recipe != null && waterMb > 0) {
				ItemStack result = recipe.result().create();
				aspects = aspects.remove(recipe.aspects());
				waterMb -= 50;
				counter = -250L;
				if (player != null) {
					CraftEventRecorder.record(player, result);
				}
				ejectItem(result);
				remaining.shrink(1);
				markDirtyAndSync();
				continue;
			}
			AspectList dissolved = io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup.get()
					.getAspects(level, input);
			if (dissolved.isEmpty()) {
				break;
			}
			aspects = aspects.add(dissolved);
			counter = -150L;
			remaining.shrink(1);
			markDirtyAndSync();
		}
		return remaining;
	}

	private ThaumcraftRecipe findRecipe(ItemStack input, ServerPlayer player) {
		if (player == null || !(level instanceof ServerLevel serverLevel)) {
			return null;
		}
		return serverLevel.getServer().getRecipeManager().getRecipes().stream()
				.map(holder -> holder.value())
				.filter(ThaumcraftRecipe.class::isInstance)
				.map(ThaumcraftRecipe.class::cast)
				.filter(recipe -> recipe.getType() == ThaumcraftRecipe.crucibleType())
				.filter(recipe -> recipe.gate().map(gate -> RecipeGateAccess.get().isUnlocked(player, gate)).orElse(true))
				.filter(recipe -> recipe.matchesCrucible(input, aspects))
				.max(Comparator.comparingInt(recipe -> recipe.aspects().visSize()))
				.orElse(null);
	}

	private void ejectItem(ItemStack result) {
		if (!(level instanceof ServerLevel serverLevel) || result.isEmpty()) {
			return;
		}
		boolean first = true;
		while (!result.isEmpty()) {
			ItemStack split = result.split(result.getMaxStackSize());
			CrucibleEjectedItemEntity entity = new CrucibleEjectedItemEntity(serverLevel,
					getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.71, getBlockPos().getZ() + 0.5, split);
			double x = first ? 0.0 : (serverLevel.getRandom().nextDouble() - 0.5) * 0.1;
			double z = first ? 0.0 : (serverLevel.getRandom().nextDouble() - 0.5) * 0.1;
			entity.setDeltaMovement(x, 0.075, z);
			serverLevel.addFreshEntity(entity);
			first = false;
		}
	}

	public void entityInside(Entity entity) {
		if (!(level instanceof ServerLevel) || !isHot() || waterMb <= 0) {
			return;
		}
		if (entity instanceof ItemEntity itemEntity) {
			if (itemEntity instanceof CrucibleEjectedItemEntity) {
				return;
			}
			ItemStack item = itemEntity.getItem();
			ServerPlayer player = itemEntity.getOwner() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
			ItemStack remaining = processItem(item, player);
			if (remaining.isEmpty()) {
				itemEntity.discard();
			} else if (remaining.getCount() != item.getCount()) {
				itemEntity.setItem(remaining);
			}
			return;
		}
		if (entity instanceof net.minecraft.world.entity.LivingEntity && ++damageCallbacks >= 10) {
			damageCallbacks = 0;
			entity.hurt(level.damageSources().inFire(), 1.0F);
		}
	}

	public void spillRandom() {
		if (aspects.isEmpty()) {
			return;
		}
		var available = aspects.aspects();
		Aspect selected = available.get(level.getRandom().nextInt(available.size()));
		aspects = aspects.remove(selected, 1);
		Aspect flux = io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries.ASPECT
				.getValue(Aspects.FLUX.identifier());
		AuraAccess.get().addFlux(level, getBlockPos(), selected.equals(flux) ? 1.0F : 0.25F, true);
		markDirtyAndSync();
	}

	public void spillRemnants() {
		int total = aspects.visSize();
		int fluxAmount = fluxAmount();
		if (waterMb == 0 && total == 0) {
			return;
		}
		waterMb = 0;
		aspects = AspectList.EMPTY;
		AuraAccess.get().addFlux(level, getBlockPos(), total * 0.25F, true);
		if (fluxAmount > 0) {
			AuraAccess.get().addFlux(level, getBlockPos(), fluxAmount * 0.75F, false);
		}
		markDirtyAndSync();
	}

	private int fluxAmount() {
		Aspect flux = io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries.ASPECT
				.getValue(Aspects.FLUX.identifier());
		return flux == null ? 0 : aspects.amount(flux);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level != null && !level.isClientSide()) {
			spillRemnants();
		}
		super.preRemoveSideEffects(pos, state);
	}
}
