package io.github.rdarkknight.thaumcraftreborn.systems.dust;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.dust.DustTrigger;
import io.github.rdarkknight.thaumcraftreborn.api.dust.DustTriggerRegistry;
import io.github.rdarkknight.thaumcraftreborn.api.recipe.ResearchGate;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.systems.recipe.CraftEventRecorder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SalisMundusSystem {
	private static final int SWAP_DELAY_TICKS = 50;
	private static final List<PendingSwap> PENDING = new ArrayList<>();
	private static final java.util.Map<ServerLevel, Long> LAST_TICK = new java.util.WeakHashMap<>();

	private SalisMundusSystem() {
	}

	public static void init() {
		ResearchGate dream = ResearchGate.parse("flag/got_dream").result().orElseThrow();
		ResearchGate firstSteps = ResearchGate.parse("FIRSTSTEPS@1").result().orElseThrow();
		ResearchGate alchemy = ResearchGate.parse("UNLOCKALCHEMY@1").result().orElseThrow();
		registerBlock("thaumonomicon_bookshelf", dream, Blocks.BOOKSHELF, () -> new ItemStack(ThaumcraftContent.THAUMONOMICON));
		registerTag("thaumonomicon_bookshelves", dream, "bookshelves", () -> new ItemStack(ThaumcraftContent.THAUMONOMICON));
		registerBlock("workbench_crafting_table", firstSteps, Blocks.CRAFTING_TABLE,
				() -> new ItemStack(ThaumcraftContent.ARCANE_WORKBENCH));
		registerTag("workbench_crafting_tables", firstSteps, "player_workstations/crafting_tables",
				() -> new ItemStack(ThaumcraftContent.ARCANE_WORKBENCH));
		registerBlock("crucible_cauldron", alchemy, Blocks.CAULDRON, () -> new ItemStack(ThaumcraftContent.CRUCIBLE));
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			PENDING.clear();
			LAST_TICK.clear();
		});
	}

	private static void registerBlock(String id, ResearchGate gate, Block input, Supplier<ItemStack> output) {
		DustTriggerRegistry.register(ThaumcraftRebornApi.id(id),
				new BlockDustTrigger(gate, state -> state.is(input), output));
	}

	private static void registerTag(String id, ResearchGate gate, String tag, Supplier<ItemStack> output) {
		TagKey<Block> key = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", tag));
		DustTriggerRegistry.register(ThaumcraftRebornApi.id(id),
				new BlockDustTrigger(gate, state -> state.is(key), output));
	}

	public static void schedule(
			ServerLevel level,
			net.minecraft.world.entity.player.Player player,
			BlockPos pos,
			DustTrigger trigger,
			BlockState original
	) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		ItemStack result = trigger.result(level, pos, original);
		CraftEventRecorder.record(serverPlayer, result);
		for (DustTrigger.Transformation transformation : trigger.transformations(level, pos, original)) {
			PENDING.add(new PendingSwap(level, transformation.pos(), transformation.original(),
					transformation.result(), level.getGameTime() + SWAP_DELAY_TICKS));
		}
	}

	private static void tick() {
		List<ServerLevel> levels = new ArrayList<>();
		PENDING.forEach(swap -> {
			if (!levels.contains(swap.level())) {
				levels.add(swap.level());
			}
		});
		for (ServerLevel level : levels) {
			long now = level.getGameTime();
			if (LAST_TICK.getOrDefault(level, Long.MIN_VALUE) == now) {
				continue;
			}
			LAST_TICK.put(level, now);
			PENDING.removeIf(swap -> {
				if (swap.level() != level || swap.dueTime() > now) {
					return false;
				}
				if (level.getBlockState(swap.pos()).equals(swap.original())) {
					if (swap.result().getItem() instanceof BlockItem blockItem) {
						level.setBlock(swap.pos(), blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
					} else {
						level.setBlock(swap.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
						BlockPos pos = swap.pos();
						ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.1,
								pos.getZ() + 0.5, swap.result().copy());
						item.setDeltaMovement(Vec3.ZERO);
						level.addFreshEntity(item);
					}
				}
				return true;
			});
		}
	}

	private record PendingSwap(
			ServerLevel level,
			BlockPos pos,
			BlockState original,
			ItemStack result,
			long dueTime
	) {
	}
}
