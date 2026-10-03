package io.github.rdarkknight.thaumcraftreborn.content.debug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.chunk.LevelChunk;

public final class DebugCommands {
	private DebugCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			var debug = Commands.literal("debug")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					.then(Commands.literal("chunk")
							.then(Commands.literal("get").executes(context -> getChunkMarker(context.getSource().getPlayerOrException())))
							.then(Commands.literal("inc").executes(context -> incrementChunkMarker(context.getSource().getPlayerOrException()))))
					.then(Commands.literal("fx")
							.then(Commands.literal("burst")
									.then(Commands.argument("count", IntegerArgumentType.integer())
											.executes(context -> burst(
													context.getSource().getPlayerOrException(),
													IntegerArgumentType.getInteger(context, "count")
											)))))
					.then(Commands.literal("data")
							.then(Commands.literal("list").executes(context -> listData(context.getSource()))))
					.then(Commands.literal("config")
							.then(Commands.literal("show").executes(context -> showConfig(context.getSource()))));
			dispatcher.register(Commands.literal("thaumcraft_reborn").then(debug));
		});
	}

	private static int getChunkMarker(ServerPlayer player) {
		LevelChunk chunk = player.level().getChunkAt(player.blockPosition());
		int marker = chunk.getAttachedOrCreate(ModAttachments.DEBUG_CHUNK_MARKER);
		player.sendSystemMessage(Component.literal("debug_chunk_marker=" + marker));
		return marker;
	}

	private static int incrementChunkMarker(ServerPlayer player) {
		LevelChunk chunk = player.level().getChunkAt(player.blockPosition());
		int marker = chunk.getAttachedOrCreate(ModAttachments.DEBUG_CHUNK_MARKER) + 1;
		chunk.setAttached(ModAttachments.DEBUG_CHUNK_MARKER, marker);
		player.sendSystemMessage(Component.literal("debug_chunk_marker=" + marker));
		return marker;
	}

	private static int burst(ServerPlayer player, int count) {
		FxDispatcher.send(player.level(), player.position(), DebugFx.DEBUG_BURST, count);
		player.sendSystemMessage(Component.literal("Sent debug burst: " + count));
		return count;
	}

	private static int listData(CommandSourceStack source) {
		DebugData.ENTRIES.serverEntries().forEach((id, value) ->
				source.sendSuccess(() -> Component.literal(id + " = " + value), false));
		return DebugData.ENTRIES.serverEntries().size();
	}

	private static int showConfig(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(ThaumcraftConfig.common(false).toString()), false);
		return 1;
	}
}
