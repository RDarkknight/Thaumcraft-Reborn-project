package io.github.rdarkknight.thaumcraftreborn.systems.aura;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class ChunkAuraAccess implements AuraAccess {
	public static final ChunkAuraAccess INSTANCE = new ChunkAuraAccess();

	private ChunkAuraAccess() {
	}

	@Override
	public float getVis(Level level, BlockPos pos) {
		return data(level, pos).map(AuraChunkData::vis).orElse(0.0F);
	}

	@Override
	public float getFlux(Level level, BlockPos pos) {
		return data(level, pos).map(AuraChunkData::flux).orElse(0.0F);
	}

	@Override
	public int getBase(Level level, BlockPos pos) {
		return data(level, pos).map(AuraChunkData::base).orElse((short) 0);
	}

	@Override
	public float getTotalAura(Level level, BlockPos pos) {
		return data(level, pos).map(chunk -> chunk.vis() + chunk.flux()).orElse(0.0F);
	}

	@Override
	public float getFluxSaturation(Level level, BlockPos pos) {
		return data(level, pos).map(chunk -> chunk.flux() / (float) chunk.base()).orElse(0.0F);
	}

	@Override
	public float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
		return drain(level, pos, amount, simulate, true);
	}

	@Override
	public float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
		return drain(level, pos, amount, simulate, false);
	}

	@Override
	public void addVis(Level level, BlockPos pos, float amount) {
		if (amount < 0.0F || Float.isNaN(amount)) {
			return;
		}
		data(level, pos).ifPresent(chunk -> set(level, pos, chunk.withVis(chunk.vis() + amount)));
	}

	@Override
	public void addFlux(Level level, BlockPos pos, float amount, boolean showEffect) {
		if (amount < 0.0F || Float.isNaN(amount)) {
			return;
		}
		data(level, pos).ifPresent(chunk -> set(level, pos, chunk.withFlux(chunk.flux() + amount)));
	}

	@Override
	public boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
		boolean hasResearch = player == null || KnowledgeAccess.get()
				.knowledge(player)
				.isResearchComplete(ThaumcraftRebornApi.id("aurapreserve"));
		return hasResearch && getVis(level, pos) / (float) getBase(level, pos) < 0.1F;
	}

	private float drain(Level level, BlockPos pos, float amount, boolean simulate, boolean vis) {
		if (!(amount > 0.0F)) {
			return 0.0F;
		}
		Optional<AuraChunkData> maybeData = data(level, pos);
		if (maybeData.isEmpty()) {
			return 0.0F;
		}
		AuraChunkData current = maybeData.get();
		float available = vis ? current.vis() : current.flux();
		float drained = Math.min(amount, available);
		if (!simulate && drained > 0.0F) {
			set(level, pos, vis
					? current.withVis(current.vis() - drained)
					: current.withFlux(current.flux() - drained));
		}
		return drained;
	}

	private Optional<AuraChunkData> data(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}
		LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk == null ? Optional.empty() : Optional.ofNullable(chunk.getAttached(ModAttachments.AURA));
	}

	private void set(Level level, BlockPos pos, AuraChunkData data) {
		ServerLevel serverLevel = (ServerLevel) level;
		LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		if (chunk != null) {
			chunk.setAttached(ModAttachments.AURA, data);
		}
	}
}
