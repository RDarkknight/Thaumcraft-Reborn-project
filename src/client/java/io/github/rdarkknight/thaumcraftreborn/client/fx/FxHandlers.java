package io.github.rdarkknight.thaumcraftreborn.client.fx;

import io.github.rdarkknight.thaumcraftreborn.api.fx.FxType;
import io.github.rdarkknight.thaumcraftreborn.client.config.ClientConfigManager;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugFx;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class FxHandlers {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<FxType<?>, FxHandler<?>> HANDLERS = new HashMap<>();
	private static final Set<FxType<?>> LOGGED_MISSING_HANDLERS = new HashSet<>();

	private FxHandlers() {
	}

	public static <D> void register(FxType<D> type, FxHandler<D> handler) {
		HANDLERS.put(type, handler);
	}

	public static void init() {
		register(DebugFx.DEBUG_BURST, DebugBurstHandler::play);
		ClientPlayNetworking.registerGlobalReceiver(FxPayload.TYPE, (payload, context) ->
				context.client().execute(() -> {
					ClientLevel level = context.client().level;
					if (level != null) {
						handle(level, payload);
					}
				}));
	}

	public static void handle(ClientLevel level, FxPayload payload) {
		if (ClientConfigManager.get().debug().logFxEvents()) {
			LOGGER.info("Received FX {}", payload.fxType());
		}

		FxHandler<Object> handler = handler(payload.fxType());
		if (handler == null) {
			if (LOGGED_MISSING_HANDLERS.add(payload.fxType())) {
				LOGGER.warn("Ignoring FX type with no client handler: {}", payload.fxType());
			}
			return;
		}

		handler.play(level, payload.origin(), payload.data());
	}

	@SuppressWarnings("unchecked")
	private static FxHandler<Object> handler(FxType<?> type) {
		return (FxHandler<Object>) HANDLERS.get(type);
	}
}
