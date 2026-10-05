package io.github.rdarkknight.thaumcraftreborn.core.attachment;

import com.mojang.serialization.Codec;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.core.aura.AuraChunkData;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerKnowledgeData;
import io.github.rdarkknight.thaumcraftreborn.core.knowledge.PlayerWarpData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModAttachments {
	public static final AttachmentType<Integer> PROBE_INTERACTIONS = AttachmentRegistry.create(
			ThaumcraftRebornApi.id("probe_interactions"),
			builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT)
					.copyOnDeath()
					.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<Integer> DEBUG_CHUNK_MARKER = AttachmentRegistry.create(
			ThaumcraftRebornApi.id("debug_chunk_marker"),
			builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT)
	);
	public static final AttachmentType<AuraChunkData> AURA = AttachmentRegistry.create(
			ThaumcraftRebornApi.id("aura"),
			builder -> builder.persistent(AuraChunkData.CODEC)
	);
	public static final AttachmentType<PlayerKnowledgeData> KNOWLEDGE = AttachmentRegistry.create(
			ThaumcraftRebornApi.id("knowledge"),
			builder -> builder
					.initializer(() -> PlayerKnowledgeData.EMPTY)
					.persistent(PlayerKnowledgeData.CODEC)
					.copyOnDeath()
					.syncWith(PlayerKnowledgeData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<PlayerWarpData> WARP = AttachmentRegistry.create(
			ThaumcraftRebornApi.id("warp"),
			builder -> builder
					.initializer(() -> PlayerWarpData.EMPTY)
					.persistent(PlayerWarpData.CODEC)
					.copyOnDeath()
					.syncWith(PlayerWarpData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	private ModAttachments() {
	}

	public static void init() {
	}
}
