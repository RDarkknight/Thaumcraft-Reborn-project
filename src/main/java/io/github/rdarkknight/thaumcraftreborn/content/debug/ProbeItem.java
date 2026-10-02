package io.github.rdarkknight.thaumcraftreborn.content.debug;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.component.ModDataComponents;
import io.github.rdarkknight.thaumcraftreborn.core.component.ProbeStamp;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ProbeItem extends Item {
	public ProbeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			ProbeStamp previous = stack.getOrDefault(ModDataComponents.PROBE_STAMP, new ProbeStamp(0, ""));
			ProbeStamp updated = new ProbeStamp(previous.uses() + 1, player.getName().getString());
			stack.set(ModDataComponents.PROBE_STAMP, updated);
			int interactions = player.getAttachedOrCreate(ModAttachments.PROBE_INTERACTIONS) + 1;
			player.setAttached(ModAttachments.PROBE_INTERACTIONS, interactions);
			player.sendOverlayMessage(Component.literal("Probe uses: " + updated.uses() + ", interactions: " + interactions));
		}
		return InteractionResult.SUCCESS;
	}
}
