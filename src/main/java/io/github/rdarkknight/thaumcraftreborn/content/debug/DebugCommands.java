package io.github.rdarkknight.thaumcraftreborn.content.debug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.Aspect;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.core.attachment.ModAttachments;
import io.github.rdarkknight.thaumcraftreborn.core.config.ThaumcraftConfig;
import io.github.rdarkknight.thaumcraftreborn.core.fx.FxDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import io.github.rdarkknight.thaumcraftreborn.core.registry.ThaumcraftRegistries;

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
							.then(Commands.literal("show").executes(context -> showConfig(context.getSource()))))
					.then(Commands.literal("aspects")
							.then(Commands.literal("list").executes(context -> listAspects(context.getSource())))
							.then(Commands.literal("hand").executes(context -> handAspects(context.getSource().getPlayerOrException())))
							.then(Commands.literal("entity")
									.then(Commands.argument("target", EntityArgument.entity())
											.executes(context -> entityAspects(context.getSource(),
													EntityArgument.getEntity(context, "target"))))))
					.then(Commands.literal("aura")
							.then(Commands.literal("get").executes(context -> getAura(context.getSource().getPlayerOrException())))
							.then(Commands.literal("drain_vis")
									.then(Commands.argument("amount", FloatArgumentType.floatArg())
											.executes(context -> drainAura(context.getSource().getPlayerOrException(),
													FloatArgumentType.getFloat(context, "amount"), true))))
							.then(Commands.literal("add_vis")
									.then(Commands.argument("amount", FloatArgumentType.floatArg())
											.executes(context -> changeAura(context.getSource().getPlayerOrException(),
													FloatArgumentType.getFloat(context, "amount"), true))))
							.then(Commands.literal("add_flux")
									.then(Commands.argument("amount", FloatArgumentType.floatArg())
											.executes(context -> changeAura(context.getSource().getPlayerOrException(),
													FloatArgumentType.getFloat(context, "amount"), false))))
							.then(Commands.literal("drain_flux")
									.then(Commands.argument("amount", FloatArgumentType.floatArg())
											.executes(context -> drainAura(context.getSource().getPlayerOrException(),
													FloatArgumentType.getFloat(context, "amount"), false)))));
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

	private static int listAspects(CommandSourceStack source) {
		for (Identifier key : ThaumcraftRegistries.ASPECT.keySet()) {
			Aspect aspect = ThaumcraftRegistries.ASPECT.getValue(key);
			source.sendSuccess(() -> Component.literal(key + " color=#" + Integer.toHexString(aspect.color())
					+ " components=" + aspect.components().stream().map(Aspect::tag).toList()), false);
		}
		return ThaumcraftRegistries.ASPECT.size();
	}

	private static int handAspects(ServerPlayer player) {
		var aspects = AspectLookup.get().getAspects(player.level(), player.getMainHandItem());
		player.sendSystemMessage(Component.literal(aspects.toString()));
		return aspects.size();
	}

	private static int entityAspects(CommandSourceStack source, Entity entity) {
		var aspects = AspectLookup.get().getAspects(entity);
		source.sendSuccess(() -> Component.literal(entity.getType() + " = " + aspects), false);
		return aspects.size();
	}

	private static int getAura(ServerPlayer player) {
		var aura = AuraAccess.get();
		var position = player.blockPosition();
		player.sendSystemMessage(Component.literal("vis=" + aura.getVis(player.level(), position)
				+ " flux=" + aura.getFlux(player.level(), position)
				+ " base=" + aura.getBase(player.level(), position)
				+ " total=" + aura.getTotalAura(player.level(), position)
				+ " saturation=" + aura.getFluxSaturation(player.level(), position)));
		return 1;
	}

	private static int changeAura(ServerPlayer player, float amount, boolean vis) {
		if (vis) {
			AuraAccess.get().addVis(player.level(), player.blockPosition(), amount);
		} else {
			AuraAccess.get().addFlux(player.level(), player.blockPosition(), amount, true);
		}
		return getAura(player);
	}

	private static int drainAura(ServerPlayer player, float amount, boolean vis) {
		float drained = vis
				? AuraAccess.get().drainVis(player.level(), player.blockPosition(), amount, false)
				: AuraAccess.get().drainFlux(player.level(), player.blockPosition(), amount, false);
		player.sendSystemMessage(Component.literal("drained=" + drained));
		return Math.round(drained);
	}
}
