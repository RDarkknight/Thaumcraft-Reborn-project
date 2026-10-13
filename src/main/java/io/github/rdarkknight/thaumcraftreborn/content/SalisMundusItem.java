package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.api.dust.DustTrigger;
import io.github.rdarkknight.thaumcraftreborn.api.dust.DustTriggerRegistry;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.RecipeGateAccess;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxDispatcher;
import io.github.rdarkknight.thaumcraftreborn.systems.dust.SalisMundusSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public final class SalisMundusItem extends Item {
	public SalisMundusItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		BlockPos pos = context.getClickedPos();
		var blockState = context.getLevel().getBlockState(pos);
		DustTrigger selected = null;
		for (DustTrigger trigger : DustTriggerRegistry.triggers()) {
			if (RecipeGateAccess.get().isUnlocked(player, trigger.gate())
					&& trigger.matches(context.getLevel(), pos, blockState)) {
				selected = trigger;
				break;
			}
		}
		if (selected == null) {
			return InteractionResult.PASS;
		}
		if (context.getLevel().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}
		if (!player.mayInteract(serverLevel, pos)) {
			return InteractionResult.FAIL;
		}
		if (!player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}
		SalisMundusSystem.schedule(serverLevel, player, pos, selected, blockState);
		Vec3 origin = Vec3.atCenterOf(pos);
		FxDispatcher.send(serverLevel, origin, SalisMundusFx.SPARK_BURST, 50);
		serverLevel.playSound((Entity) null, pos, SalisMundusSoundEvents.USE, SoundSource.PLAYERS, 0.33F,
				(float) (1.0 + serverLevel.getRandom().nextGaussian() * 0.05));
		return InteractionResult.SUCCESS;
	}
}
