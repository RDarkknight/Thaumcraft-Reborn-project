package io.github.rdarkknight.thaumcraftreborn.core.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CommonConfig(Debug debug, Misc misc) {
	public static final CommonConfig DEFAULT = new CommonConfig(Debug.DEFAULT, Misc.DEFAULT);
	public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Debug.CODEC.optionalFieldOf("debug", Debug.DEFAULT).forGetter(CommonConfig::debug),
			Misc.CODEC.optionalFieldOf("misc", Misc.DEFAULT).forGetter(CommonConfig::misc)
	).apply(instance, CommonConfig::new));

	public record Debug(boolean verboseDataLogging) {
		private static final Debug DEFAULT = new Debug(false);
		private static final Codec<Debug> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("verbose_data_logging", false).forGetter(Debug::verboseDataLogging)
		).apply(instance, Debug::new));
	}

	public record Misc(boolean wussMode) {
		private static final Misc DEFAULT = new Misc(false);
		private static final Codec<Misc> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("wuss_mode", false).forGetter(Misc::wussMode)
		).apply(instance, Misc::new));
	}
}
