package io.github.rdarkknight.thaumcraftreborn.core.data;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import com.mojang.logging.LogUtils;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public final class SyncedDataLoader<T> extends SimpleJsonResourceReloadListener<T> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Identifier id;
	private final Codec<T> codec;
	private final FileToIdConverter fileToIdConverter;
	private final CustomPacketPayload.Type<DataSyncPayload<T>> payloadType;
	private final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, T>> entriesStreamCodec;
	private final StreamCodec<RegistryFriendlyByteBuf, DataSyncPayload<T>> payloadCodec;
	private final List<Runnable> serverEntriesListeners = new CopyOnWriteArrayList<>();
	private final List<Runnable> clientEntriesListeners = new CopyOnWriteArrayList<>();
	private final AtomicInteger clientSyncCount = new AtomicInteger();
	private volatile HolderLookup.Provider registryLookup;
	private volatile Map<Identifier, T> serverEntries = Map.of();
	private volatile Map<Identifier, T> clientEntries = Map.of();

	public SyncedDataLoader(Identifier id, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		this(id, codec, streamCodec, FileToIdConverter.json("thaumcraft_reborn/" + id.getPath()));
	}

	private SyncedDataLoader(
			Identifier id,
			Codec<T> codec,
			StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
			FileToIdConverter fileToIdConverter
	) {
		super(codec, fileToIdConverter);
		this.id = id;
		this.codec = codec;
		this.fileToIdConverter = fileToIdConverter;
		this.payloadType = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(id.getNamespace(), "data_sync/" + id.getPath()));
		this.entriesStreamCodec = ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, streamCodec);
		this.payloadCodec = StreamCodec.of(
				(buffer, payload) -> this.entriesStreamCodec.encode(buffer, payload.entries()),
				buffer -> new DataSyncPayload<>(this.payloadType, this.entriesStreamCodec.decode(buffer))
		);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, this);
		SyncedDataLoaders.register(this);
	}

	public Identifier id() {
		return id;
	}

	public Map<Identifier, T> serverEntries() {
		return serverEntries;
	}

	public Map<Identifier, T> clientEntries() {
		return clientEntries;
	}

	public int clientSyncCount() {
		return clientSyncCount.get();
	}

	public Map<Identifier, T> entries(boolean clientSide) {
		return clientSide ? clientEntries : serverEntries;
	}

	public CustomPacketPayload.Type<DataSyncPayload<T>> payloadType() {
		return payloadType;
	}

	public StreamCodec<RegistryFriendlyByteBuf, DataSyncPayload<T>> payloadCodec() {
		return payloadCodec;
	}

	public void replaceClientEntries(Map<Identifier, T> entries) {
		clientEntries = Map.copyOf(entries);
		clientSyncCount.incrementAndGet();
		clientEntriesListeners.forEach(Runnable::run);
		if (ThaumcraftConfig.common(true).debug().verboseDataLogging()) {
			clientEntries.keySet().forEach(entryId -> LOGGER.info("Received data entry {} from loader {}", entryId, id));
		}
	}

	public void clearClientEntries() {
		clientEntries = Map.of();
		clientEntriesListeners.forEach(Runnable::run);
	}

	public void addServerEntriesListener(Runnable listener) {
		serverEntriesListeners.add(listener);
	}

	public void addClientEntriesListener(Runnable listener) {
		clientEntriesListeners.add(listener);
	}

	public void syncTo(ServerPlayer player) {
		ServerPlayNetworking.send(player, new DataSyncPayload<>(payloadType, serverEntries));
	}

	public void syncToAll(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			syncTo(player);
		}
	}

	@Override
	public void prepareSharedState(PreparableReloadListener.SharedState sharedState) {
		registryLookup = sharedState.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
	}

	@Override
	protected Map<Identifier, T> prepare(ResourceManager manager, ProfilerFiller profiler) {
		return loadEntries(manager, registryLookup);
	}

	private Map<Identifier, T> loadEntries(ResourceManager manager, HolderLookup.Provider registryLookup) {
		Map<Identifier, T> entries = new HashMap<>();
		Map<Identifier, List<Resource>> resourceStacks = fileToIdConverter.listMatchingResourceStacks(manager);

		for (Map.Entry<Identifier, List<Resource>> resourceEntry : resourceStacks.entrySet()) {
			Identifier entryId = fileToIdConverter.fileToId(resourceEntry.getKey());
			List<Resource> stack = resourceEntry.getValue();
			Resource winner = stack.getFirst();
			if (stack.size() > 1) {
				LOGGER.info(
						"Data entry {} is defined by packs {}; {} wins",
						entryId,
						String.join(", ", stack.stream().map(Resource::sourcePackId).toList()),
						winner.sourcePackId()
				);
			}

			try (Reader reader = winner.openAsReader()) {
				DataResult<T> result = codec.parse(
						RegistryOps.create(JsonOps.INSTANCE, registryLookup),
						StrictJsonParser.parse(reader)
				);
				Optional<DataResult.Error<T>> error = result.error();
				if (error.isPresent()) {
					LOGGER.error("Couldn't parse data entry {} from pack {}: {}", entryId, winner.sourcePackId(), error.get().message());
					continue;
				}

				result.result().ifPresent(value -> {
					entries.put(entryId, value);
					if (ThaumcraftConfig.common(false).debug().verboseDataLogging()) {
						LOGGER.info("Loaded data entry {} from pack {}", entryId, winner.sourcePackId());
					}
				});
			} catch (Exception exception) {
				LOGGER.error("Couldn't parse data entry {} from pack {}", entryId, winner.sourcePackId(), exception);
			}
		}

		return entries;
	}

	@Override
	protected void apply(Map<Identifier, T> entries, ResourceManager manager, ProfilerFiller profiler) {
		serverEntries = Map.copyOf(entries);
		serverEntriesListeners.forEach(Runnable::run);
	}

	public record DataSyncPayload<T>(
			CustomPacketPayload.Type<DataSyncPayload<T>> payloadType,
			Map<Identifier, T> entries
	) implements CustomPacketPayload {
		public DataSyncPayload {
			entries = Map.copyOf(entries);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return payloadType;
		}
	}
}
