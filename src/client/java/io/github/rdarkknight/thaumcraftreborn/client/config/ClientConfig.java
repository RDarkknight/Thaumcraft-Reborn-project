package io.github.rdarkknight.thaumcraftreborn.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClientConfig(ClientDebug debug) {
	public static final ClientConfig DEFAULT = new ClientConfig(ClientDebug.DEFAULT);
	public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ClientDebug.CODEC.optionalFieldOf("debug", ClientDebug.DEFAULT).forGetter(ClientConfig::debug)
	).apply(instance, ClientConfig::new));

	public record ClientDebug(boolean logFxEvents) {
		private static final ClientDebug DEFAULT = new ClientDebug(false);
		private static final Codec<ClientDebug> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("log_fx_events", false).forGetter(ClientDebug::logFxEvents)
		).apply(instance, ClientDebug::new));
	}
}
