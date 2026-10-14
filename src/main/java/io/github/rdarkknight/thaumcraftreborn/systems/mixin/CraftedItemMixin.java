package io.github.rdarkknight.thaumcraftreborn.systems.mixin;

import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class CraftedItemMixin {
	@Inject(method = "onTake", at = @At("HEAD"))
	private void thaumcraft_reborn$recordCraftedItem(Player player, ItemStack stack, CallbackInfo callbackInfo) {
		if (player instanceof ServerPlayer serverPlayer) {
			CraftEventRecorder.record(serverPlayer, stack);
		}
	}
}
