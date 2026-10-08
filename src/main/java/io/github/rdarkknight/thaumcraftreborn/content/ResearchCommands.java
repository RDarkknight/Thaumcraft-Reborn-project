package io.github.rdarkknight.thaumcraftreborn.content;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchMeta;
import io.github.rdarkknight.thaumcraftreborn.api.research.WarpType;
import java.util.Optional;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class ResearchCommands {
	private ResearchCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			var research = Commands.literal("research")
					.requires(ResearchCommands::canManage)
					.then(Commands.literal("list").executes(context -> listLoaded(context.getSource())))
					.then(Commands.argument("targets", EntityArgument.players())
							.then(Commands.literal("list").executes(context -> listPlayerResearch(
									context.getSource(),
									EntityArgument.getPlayers(context, "targets")
							)))
							.then(Commands.literal("all").executes(context -> giveAll(
									context.getSource(),
									EntityArgument.getPlayers(context, "targets")
							)))
							.then(Commands.literal("reset").executes(context -> reset(
									context.getSource(),
									EntityArgument.getPlayers(context, "targets")
							)))
							.then(Commands.literal("grant")
									.then(Commands.argument("key", IdentifierArgument.id())
											.suggests((context, builder) -> SharedSuggestionProvider.suggest(
													ResearchAccess.get().entries(false).keySet().stream()
															.map(Identifier::toString).toList(),
													builder
											))
											.executes(context -> grant(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IdentifierArgument.getId(context, "key")
											))))
							.then(Commands.literal("revoke")
									.then(Commands.argument("key", IdentifierArgument.id())
											.suggests((context, builder) -> SharedSuggestionProvider.suggest(
													ResearchAccess.get().entries(false).keySet().stream()
															.map(Identifier::toString).toList(),
													builder
											))
											.executes(context -> revoke(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IdentifierArgument.getId(context, "key")
											)))));
			var warp = Commands.literal("warp")
					.requires(ResearchCommands::canManage)
					.then(Commands.argument("targets", EntityArgument.players())
							.then(Commands.literal("get").executes(context -> getWarp(
									context.getSource(),
									EntityArgument.getPlayers(context, "targets")
							)))
							.then(Commands.literal("add")
									.then(Commands.argument("amount", IntegerArgumentType.integer())
											.executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IntegerArgumentType.getInteger(context, "amount"),
													WarpType.NORMAL,
													false
											))
											.then(Commands.literal("permanent").executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IntegerArgumentType.getInteger(context, "amount"),
													WarpType.PERMANENT,
													false
											)))
											.then(Commands.literal("temporary").executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IntegerArgumentType.getInteger(context, "amount"),
													WarpType.TEMPORARY,
													false
											)))))
							.then(Commands.literal("set")
									.then(Commands.argument("amount", IntegerArgumentType.integer())
											.executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IntegerArgumentType.getInteger(context, "amount"),
													WarpType.NORMAL,
													true
											))
											.then(Commands.literal("permanent").executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
													IntegerArgumentType.getInteger(context, "amount"),
													WarpType.PERMANENT,
													true
											)))
											.then(Commands.literal("temporary").executes(context -> changeWarp(
													context.getSource(),
													EntityArgument.getPlayers(context, "targets"),
														IntegerArgumentType.getInteger(context, "amount"),
														WarpType.TEMPORARY,
														true
												))))));
			var knowledge = Commands.literal("knowledge")
					.requires(ResearchCommands::canManage)
					.then(Commands.argument("targets", EntityArgument.players())
							.then(Commands.literal("get").executes(context -> getKnowledge(
									context.getSource(),
									EntityArgument.getPlayers(context, "targets")
							))));
			dispatcher.register(Commands.literal("thaumcraft_reborn")
					.then(research)
					.then(warp)
					.then(knowledge));
		});
	}

	private static boolean canManage(CommandSourceStack source) {
		return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	private static int listLoaded(CommandSourceStack source) {
		ResearchAccess.get().entries(false).keySet().stream().sorted().forEach(key ->
				source.sendSuccess(() -> Component.literal(key.toString()), false));
		return ResearchAccess.get().entries(false).size();
	}

	private static int listPlayerResearch(CommandSourceStack source, Iterable<ServerPlayer> targets) {
		for (ServerPlayer target : targets) {
			source.sendSuccess(() -> Component.literal(target.getGameProfile().name() + ": "
					+ KnowledgeAccess.get().knowledge(target).getResearchList().stream()
							.sorted().map(Identifier::toString).reduce((left, right) -> left + ", " + right).orElse("")), false);
		}
		return 1;
	}

	private static int giveAll(CommandSourceStack source, Iterable<ServerPlayer> targets) {
		int changed = 0;
		for (ServerPlayer target : targets) {
			for (Identifier key : ResearchAccess.get().entries(false).keySet()) {
				if (ResearchAccess.get().giveRecursiveResearch(target, key)) {
					changed++;
				}
			}
			source.sendSuccess(() -> Component.literal("Granted all loaded research to " + target.getGameProfile().name()), true);
		}
		return changed;
	}

	private static int reset(CommandSourceStack source, Iterable<ServerPlayer> targets) {
		int count = 0;
		for (ServerPlayer target : targets) {
			KnowledgeAccess.get().clearKnowledge(target);
			ResearchAccess.get().entries(false).forEach((key, entry) -> {
				if (entry.meta().contains(ResearchMeta.AUTOUNLOCK)) {
					ResearchAccess.get().completeResearch(target, key, false);
				}
			});
			source.sendSuccess(() -> Component.literal("Reset research for " + target.getGameProfile().name()), true);
			count++;
		}
		return count;
	}

	private static int grant(CommandSourceStack source, Iterable<ServerPlayer> targets, Identifier key) {
		if (ResearchAccess.get().entry(key, false).isEmpty()) {
			source.sendFailure(Component.literal("Research does not exist: " + key));
			return 0;
		}
		int count = 0;
		for (ServerPlayer target : targets) {
			if (ResearchAccess.get().giveRecursiveResearch(target, key)) {
				count++;
			}
		}
		source.sendSuccess(() -> Component.literal("Granted " + key + " and requisites"), true);
		return count;
	}

	private static int revoke(CommandSourceStack source, Iterable<ServerPlayer> targets, Identifier key) {
		if (ResearchAccess.get().entry(key, false).isEmpty()) {
			source.sendFailure(Component.literal("Research does not exist: " + key));
			return 0;
		}
		int count = 0;
		for (ServerPlayer target : targets) {
			if (ResearchAccess.get().revokeRecursiveResearch(target, key)) {
				count++;
			}
		}
		source.sendSuccess(() -> Component.literal("Revoked " + key + " and dependent research"), true);
		return count;
	}

	private static int getWarp(CommandSourceStack source, Iterable<ServerPlayer> targets) {
		for (ServerPlayer target : targets) {
			var warp = KnowledgeAccess.get().warp(target);
			source.sendSuccess(() -> Component.literal(target.getGameProfile().name() + " warp: permanent="
					+ warp.get(WarpType.PERMANENT) + ", normal=" + warp.get(WarpType.NORMAL) + ", temporary="
					+ warp.get(WarpType.TEMPORARY) + ", counter=" + warp.getCounter()), false);
		}
		return 1;
	}

	private static int changeWarp(
			CommandSourceStack source,
			Iterable<ServerPlayer> targets,
			int amount,
			WarpType type,
			boolean set
	) {
		for (ServerPlayer target : targets) {
			if (set) {
				KnowledgeAccess.get().setWarp(target, type, amount);
			} else {
				KnowledgeAccess.get().addWarp(target, type, amount);
			}
		}
		source.sendSuccess(() -> Component.literal((set ? "Set " : "Added ") + amount + " " + type.name().toLowerCase()
				+ " warp for " + (targets instanceof java.util.Collection<?> collection ? collection.size() : "selected players")), true);
		return amount;
	}

	private static int getKnowledge(CommandSourceStack source, Iterable<ServerPlayer> targets) {
		for (ServerPlayer target : targets) {
			var knowledge = KnowledgeAccess.get().knowledge(target);
			source.sendSuccess(() -> Component.literal(target.getGameProfile().name() + " knowledge: theory="
					+ knowledge.getKnowledge(KnowledgeType.THEORY, Optional.empty()) + " (" + knowledge.getKnowledgeRaw(KnowledgeType.THEORY, Optional.empty())
					+ " raw), observation=" + knowledge.getKnowledge(KnowledgeType.OBSERVATION, Optional.empty()) + " ("
					+ knowledge.getKnowledgeRaw(KnowledgeType.OBSERVATION, Optional.empty()) + " raw)"), false);
		}
		return 1;
	}
}
