package io.github.rdarkknight.thaumcraftreborn.content;

import io.github.rdarkknight.thaumcraftreborn.api.research.ScanningAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThaumometerItem extends Item {
	public ThaumometerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			Vec3 start = player.getEyePosition();
			Vec3 end = start.add(player.getViewVector(1.0F).scale(9.0));
			BlockHitResult blockHit = player.pick(9.0, 0.0F, false) instanceof BlockHitResult hit
					? hit
					: null;
			double blockDistance = blockHit == null || blockHit.getType() != HitResult.Type.BLOCK
					? Double.MAX_VALUE
					: start.distanceTo(blockHit.getLocation());
			Entity entity = findTarget(level, player, start, end);
			if (entity != null && start.distanceTo(entity.getBoundingBox().getCenter()) < blockDistance) {
				report(serverPlayer, ScanningAccess.get().scanEntity(serverPlayer, entity));
			} else if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
				report(serverPlayer, ScanningAccess.get().scanBlock(serverPlayer, blockHit.getBlockPos()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	private static Entity findTarget(Level level, Player player, Vec3 start, Vec3 end) {
		AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
		return level.getEntities(player, searchBox, Entity::isPickable).stream()
				.filter(entity -> entity.getBoundingBox().inflate(0.3).clip(start, end).isPresent())
				.min(java.util.Comparator.comparingDouble(entity -> start.distanceTo(entity.getBoundingBox().getCenter())))
				.orElse(null);
	}

	private static void report(ServerPlayer player, ScanningAccess.ScanResult result) {
		String key = switch (result) {
			case SCANNED -> "message.thaumcraft_reborn.scan_found";
			case ALREADY_KNOWN -> "message.thaumcraft_reborn.scan_known";
			case NOT_SCANNABLE -> "message.thaumcraft_reborn.scan_unknown";
		};
		player.sendOverlayMessage(Component.translatable(key));
	}
}
