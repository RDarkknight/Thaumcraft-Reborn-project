package io.github.rdarkknight.thaumcraftreborn.core.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CommonConfig(Debug debug) {
	public static final CommonConfig DEFAULT = new CommonConfig(Debug.DEFAULT);
	public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Debug.CODEC.optionalFieldOf("debug", Debug.DEFAULT).forGetter(CommonConfig::debug)
	).apply(instance, CommonConfig::new));

	public record Debug(boolean verboseDataLogging) {
		private static final Debug DEFAULT = new Debug(false);
		private static final Codec<Debug> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("verbose_data_logging", false).forGetter(Debug::verboseDataLogging)
		).apply(instance, Debug::new));
	}
}
