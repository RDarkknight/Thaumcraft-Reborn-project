package io.github.rdarkknight.thaumcraftreborn.core.network;

import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModPayloads {
	private ModPayloads() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(ProbePingPayload.TYPE, ProbePingPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ProbePongPayload.TYPE, ProbePongPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ProbePingPayload.TYPE, (payload, context) -> {
			int interactions = context.player().getAttachedOrCreate(ModAttachments.PROBE_INTERACTIONS);
			ServerPlayNetworking.send(context.player(), new ProbePongPayload(payload.nonce(), interactions));
		});
	}
}
