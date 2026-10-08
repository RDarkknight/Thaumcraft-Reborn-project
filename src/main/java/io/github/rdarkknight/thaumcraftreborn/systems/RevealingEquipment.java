package io.github.rdarkknight.thaumcraftreborn.systems;

import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.accessory.AccessoryAccess;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class RevealingEquipment {
	private RevealingEquipment() {
	}

	public static boolean isRevealing(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(ThaumcraftContent.GOGGLES)
				|| AccessoryAccess.get().isEquipped(entity, ThaumcraftContent.GOGGLES);
	}
}
