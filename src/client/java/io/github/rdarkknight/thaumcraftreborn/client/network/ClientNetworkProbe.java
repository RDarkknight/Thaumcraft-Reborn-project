package io.github.rdarkknight.thaumcraftreborn.client.network;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.core.network.ProbePingPayload;
import io.github.rdarkknight.thaumcraftreborn.core.network.ProbePongPayload;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworkProbe {
	private static final AtomicInteger LAST_SENT_NONCE = new AtomicInteger(Integer.MIN_VALUE);
	private static final AtomicInteger LAST_RECEIVED_NONCE = new AtomicInteger(Integer.MIN_VALUE);
	private static final AtomicInteger LAST_INTERACTIONS = new AtomicInteger();

	private ClientNetworkProbe() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ProbePongPayload.TYPE, (payload, context) -> receive(payload));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			int nonce = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
			LAST_SENT_NONCE.set(nonce);
			ClientPlayNetworking.send(new ProbePingPayload(nonce));
		});
	}

	public static int lastSentNonce() {
		return LAST_SENT_NONCE.get();
	}

	public static int lastReceivedNonce() {
		return LAST_RECEIVED_NONCE.get();
	}

	public static int lastInteractions() {
		return LAST_INTERACTIONS.get();
	}

	private static void receive(ProbePongPayload payload) {
		LAST_RECEIVED_NONCE.set(payload.nonce());
		LAST_INTERACTIONS.set(payload.interactions());
		ThaumcraftReborn.LOGGER.info("[thaumcraft_reborn] network probe pong nonce={} interactions={}", payload.nonce(), payload.interactions());
	}
}
