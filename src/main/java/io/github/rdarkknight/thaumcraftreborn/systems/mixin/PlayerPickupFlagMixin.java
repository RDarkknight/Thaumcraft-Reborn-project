package io.github.rdarkknight.thaumcraftreborn.systems.mixin;

import io.github.rdarkknight.thaumcraftreborn.systems.PickupResearchFlags;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class PlayerPickupFlagMixin {
	@Shadow
	private int pickupDelay;

	@Shadow
	private UUID target;

	@Inject(method = "playerTouch", at = @At("HEAD"))
	private void thaumcraft_reborn$grantPickupFlags(Player player, CallbackInfo callbackInfo) {
		if (player instanceof ServerPlayer serverPlayer) {
			PickupResearchFlags.onPickupAttempt(serverPlayer, (ItemEntity) (Object) this, this.pickupDelay, this.target);
		}
	}
}
