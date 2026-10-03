package io.github.rdarkknight.thaumcraftreborn.core.fx;

import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import java.util.Collection;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class FxDispatcher {
	private FxDispatcher() {
	}

	public static <D> void send(ServerLevel level, Vec3 origin, FxType<D> type, D data) {
		FxPayload payload = FxPayload.of(type, data, origin);
		ChunkPos chunkPos = ChunkPos.containing(BlockPos.containing(origin));
		Collection<ServerPlayer> players = PlayerLookup.tracking(level, chunkPos);
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
