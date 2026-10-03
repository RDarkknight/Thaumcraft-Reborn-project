package io.github.rdarkknight.thaumcraftreborn.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class ConfigFiles {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigFiles() {
	}

	public static <T> T load(Path path, Codec<T> codec, T defaults) {
		if (!Files.exists(path)) {
			try {
				write(path, codec, defaults);
			} catch (IOException | RuntimeException exception) {
				LOGGER.error("Failed to write default config {}; using defaults", path, exception);
			}

			return defaults;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement json = GSON.fromJson(reader, JsonElement.class);
			if (json == null || json.isJsonNull()) {
				throw new JsonParseException("Config JSON is empty");
			}

			return codec.parse(JsonOps.INSTANCE, json).getOrThrow();
		} catch (IOException | RuntimeException exception) {
			LOGGER.error("Failed to load config {}; using defaults", path, exception);
			return defaults;
		}
	}

	private static <T> void write(Path path, Codec<T> codec, T value) throws IOException {
		JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
		Files.createDirectories(path.getParent());
		Files.writeString(path, GSON.toJson(json), StandardCharsets.UTF_8);
	}
}
