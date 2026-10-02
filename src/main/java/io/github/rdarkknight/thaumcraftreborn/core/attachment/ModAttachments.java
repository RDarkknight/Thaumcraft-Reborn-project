package io.github.rdarkknight.thaumcraftreborn.core.attachment;

import com.mojang.serialization.Codec;
import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModAttachments {
	public static final AttachmentType<Integer> PROBE_INTERACTIONS = AttachmentRegistry.create(
			ThaumcraftReborn.id("probe_interactions"),
			builder -> builder
					.initializer(() -> 0)
					.persistent(Codec.INT)
					.copyOnDeath()
					.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
	);

	private ModAttachments() {
	}

	public static void init() {
	}
}
