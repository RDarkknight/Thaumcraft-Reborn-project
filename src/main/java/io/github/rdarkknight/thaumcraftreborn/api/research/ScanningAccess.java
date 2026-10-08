package io.github.rdarkknight.thaumcraftreborn.api.research;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface ScanningAccess {
	ScanningAccess NONE = new ScanningAccess() {
		@Override
		public ScanResult scanBlock(ServerPlayer player, BlockPos pos) {
			return ScanResult.NOT_SCANNABLE;
		}

		@Override
		public ScanResult scanEntity(ServerPlayer player, Entity entity) {
			return ScanResult.NOT_SCANNABLE;
		}

		@Override
		public ScanResult scanItem(ServerPlayer player, ItemStack stack) {
			return ScanResult.NOT_SCANNABLE;
		}

		@Override
		public ScanResult scanInventorySlot(ServerPlayer player, int containerId, int slotId) {
			return ScanResult.NOT_SCANNABLE;
		}
	};

	static ScanningAccess get() {
		return Holder.INSTANCE;
	}

	static void install(ScanningAccess access) {
		Holder.INSTANCE = Objects.requireNonNull(access);
	}

	ScanResult scanBlock(ServerPlayer player, BlockPos pos);

	ScanResult scanEntity(ServerPlayer player, Entity entity);

	ScanResult scanItem(ServerPlayer player, ItemStack stack);

	ScanResult scanInventorySlot(ServerPlayer player, int containerId, int slotId);

	enum ScanResult {
		SCANNED,
		ALREADY_KNOWN,
		NOT_SCANNABLE
	}

	final class Holder {
		private static volatile ScanningAccess INSTANCE = NONE;

		private Holder() {
		}
	}
}
