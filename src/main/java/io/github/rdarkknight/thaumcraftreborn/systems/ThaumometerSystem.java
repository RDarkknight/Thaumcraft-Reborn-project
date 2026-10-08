package io.github.rdarkknight.thaumcraftreborn.systems;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aura.AuraAccess;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ScanningAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.network.AuraSyncPayload;
import io.github.rdarkknight.thaumcraftreborn.core.network.ScanSlotPayload;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;

public final class ThaumometerSystem {
	private static final int SLOT_SCAN_INTERVAL = 5;
	private static final Map<UUID, Integer> LAST_SLOT_SCAN = new ConcurrentHashMap<>();

	private ThaumometerSystem() {
	}

	public static void init() {
		ScanningAccess.install(new Access());
		ServerPlayNetworking.registerGlobalReceiver(ScanSlotPayload.TYPE, (payload, context) ->
				context.server().execute(() -> scanSlotPayload(context.player(), payload)));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				LAST_SLOT_SCAN.remove(handler.player.getUUID()));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.tickCount % 20 == 0 && shouldSyncAura(player)) {
					syncAura(player);
				}
			}
		});
	}

	public static boolean shouldSyncAura(ServerPlayer player) {
		return player.getMainHandItem().is(ThaumcraftContent.THAUMOMETER)
				|| player.getInventory().getItem(0).is(ThaumcraftContent.THAUMOMETER);
	}

	public static boolean canScanInventory(ServerPlayer player) {
		return player.getMainHandItem().is(ThaumcraftContent.THAUMOMETER)
				|| player.getOffhandItem().is(ThaumcraftContent.THAUMOMETER);
	}

	public static boolean isThaumometerActive(ServerPlayer player) {
		return canScanInventory(player);
	}

	private static void scanSlotPayload(ServerPlayer player, ScanSlotPayload payload) {
		if (payload.containerId() < 0 || payload.slotId() < 0
				|| player.containerMenu.containerId != payload.containerId()
				|| !canScanInventory(player)) {
			return;
		}
		int tick = player.tickCount;
		Integer lastTick = LAST_SLOT_SCAN.get(player.getUUID());
		if (lastTick != null && tick - lastTick < SLOT_SCAN_INTERVAL) {
			return;
		}
		if (payload.slotId() >= player.containerMenu.slots.size()) {
			return;
		}
		ItemStack current = player.containerMenu.getSlot(payload.slotId()).getItem();
		if (current.isEmpty()) {
			return;
		}
		LAST_SLOT_SCAN.put(player.getUUID(), tick);
		report(player, ScanningAccess.get().scanItem(player, current.copy()));
	}

	private static void syncAura(ServerPlayer player) {
		BlockPos pos = player.blockPosition();
		AuraAccess aura = AuraAccess.get();
		int base = aura.getBase(player.level(), pos);
		float vis = aura.getVis(player.level(), pos);
		float flux = aura.getFlux(player.level(), pos);
		if ((flux > vis || flux > base / 3) && !KnowledgeAccess.get().knowledge(player)
				.isResearchKnown(ThaumcraftRebornApi.id("flux"))) {
			if (ResearchAccess.get().progressResearch(player, ThaumcraftRebornApi.id("flux"), true)) {
				KnowledgeAccess.get().setResearchFlag(
						player,
						ThaumcraftRebornApi.id("flux"),
						io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag.POPUP
				);
			}
			player.sendOverlayMessage(Component.translatable("message.thaumcraft_reborn.flux_warning"));
		}
		ServerPlayNetworking.send(player, new AuraSyncPayload(base, vis, flux));
	}

	public static void report(ServerPlayer player, ScanningAccess.ScanResult result) {
		String key = switch (result) {
			case SCANNED -> "message.thaumcraft_reborn.scan_found";
			case ALREADY_KNOWN -> "message.thaumcraft_reborn.scan_known";
			case NOT_SCANNABLE -> "message.thaumcraft_reborn.scan_unknown";
		};
		player.sendOverlayMessage(Component.translatable(key));
	}

	private static final class Access implements ScanningAccess {
		@Override
		public ScanResult scanBlock(ServerPlayer player, BlockPos pos) {
			Block block = player.level().getBlockState(pos).getBlock();
			Identifier target = BuiltInRegistries.BLOCK.getKey(block);
			String specialPath = switch (target.getPath()) {
				case "ore_amber" -> "scan/ore_amber";
				case "ore_cinnabar" -> "scan/ore_cinnabar";
				case "crystal_aer", "crystal_terra", "crystal_ignis", "crystal_aqua", "crystal_ordo", "crystal_perditio" ->
						"scan/ore_crystal";
				default -> null;
			};
			boolean scanned = specialPath != null && addKey(player, ThaumcraftRebornApi.id(specialPath));
			ScanResult generic = scanItem(player, new ItemStack(block.asItem()));
			if (player.level().getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
				int inspected = Math.min(container.getContainerSize(), 100);
				for (int slot = 0; slot < inspected; slot++) {
					ScanResult contained = scanItem(player, container.getItem(slot));
					if (contained == ScanResult.SCANNED) {
						generic = contained;
					}
				}
			}
			if (scanned) {
				return ScanResult.SCANNED;
			}
			return specialPath != null && generic == ScanResult.NOT_SCANNABLE
					? ScanResult.ALREADY_KNOWN
					: generic;
		}

		@Override
		public ScanResult scanEntity(ServerPlayer player, Entity entity) {
			if (entity instanceof ItemEntity itemEntity) {
				return scanItem(player, itemEntity.getItem());
			}
			ScanResult generic = scanAspectTarget(
					player,
					AspectLookupBridge.entityAspects(entity),
					BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())
			);
			return entity instanceof LivingEntity living ? grantPotionKeys(player, living, generic) : generic;
		}

		@Override
		public ScanResult scanItem(ServerPlayer player, ItemStack stack) {
			if (stack.isEmpty()) {
				return ScanResult.NOT_SCANNABLE;
			}
			ScanResult generic = scanAspectTarget(
					player,
					AspectLookup.get().getAspects(player.level(), stack),
					BuiltInRegistries.ITEM.getKey(stack.getItem())
			);
			ScanResult special = grantItemKeys(player, stack);
			return combine(generic, special);
		}

		@Override
		public ScanResult scanInventorySlot(ServerPlayer player, int containerId, int slotId) {
			if (containerId != player.containerMenu.containerId || slotId < 0 || slotId >= player.containerMenu.slots.size()) {
				return ScanResult.NOT_SCANNABLE;
			}
			return scanItem(player, player.containerMenu.getSlot(slotId).getItem());
		}

		private ScanResult scanAspectTarget(
				ServerPlayer player,
				io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList aspects,
				Identifier target
		) {
			if (aspects.isEmpty()) {
				return ScanResult.NOT_SCANNABLE;
			}
			boolean scanned = addKey(player, ThaumcraftRebornApi.id("scan/" + scanPath(target)));
			if (scanned) {
				grantObservation(player, aspects);
			}
			for (var aspect : aspects.aspects()) {
				if (addKey(player, ThaumcraftRebornApi.id("scan/aspect/" + aspect.tag()))) {
					scanned = true;
					grantAspectObservation(player);
				}
			}
			return scanned ? ScanResult.SCANNED : ScanResult.ALREADY_KNOWN;
		}

		private boolean addKey(ServerPlayer player, net.minecraft.resources.Identifier key) {
			boolean added = KnowledgeAccess.get().addResearch(player, key);
			if (added && ResearchAccess.get().entry(key, false).isPresent()) {
				KnowledgeAccess.get().setResearchFlag(
						player,
						key,
						io.github.rdarkknight.thaumcraftreborn.api.research.ResearchFlag.POPUP
				);
			}
			return added;
		}

		private void grantObservation(ServerPlayer player, io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList aspects) {
			ResearchAccess.get().categories(false).forEach((categoryId, category) -> {
				double total = category.formula().aspects().stream()
						.mapToDouble(aspect -> aspects.amount(aspect) * (category.formula().amount(aspect) / 10.0))
						.sum();
				int amount = total <= 0.0 ? 0 : (int) Math.ceil(Math.sqrt(total));
				if (amount > 0) {
					KnowledgeAccess.get().addKnowledge(
							player,
							io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType.OBSERVATION,
							java.util.Optional.of(categoryId),
							amount
					);
				}
			});
		}

		private void grantAspectObservation(ServerPlayer player) {
			for (String category : new String[]{"auromancy", "basics", "alchemy"}) {
				KnowledgeAccess.get().addKnowledge(
						player,
						io.github.rdarkknight.thaumcraftreborn.api.research.KnowledgeType.OBSERVATION,
						java.util.Optional.of(ThaumcraftRebornApi.id(category)),
						1
				);
			}
		}

		private String scanPath(Identifier target) {
			if (target.getNamespace().equals(ThaumcraftRebornApi.MOD_ID)
					|| target.getNamespace().equals("minecraft")) {
				return target.getPath();
			}
			return target.getNamespace() + "/" + target.getPath();
		}

		private ScanResult grantItemKeys(ServerPlayer player, ItemStack stack) {
			boolean added = false;
			ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
			if (enchantments != null) {
				for (var enchantment : enchantments.keySet()) {
					added |= enchantment.unwrapKey().map(key -> addKey(player,
							ThaumcraftRebornApi.id("scan/" + scanPath(key.identifier())))).orElse(false);
				}
			}
			ItemEnchantments storedEnchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
			if (storedEnchantments != null) {
				for (var enchantment : storedEnchantments.keySet()) {
					added |= enchantment.unwrapKey().map(key -> addKey(player,
							ThaumcraftRebornApi.id("scan/" + scanPath(key.identifier())))).orElse(false);
				}
			}
			PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
			if (potion != null) {
				for (MobEffectInstance effect : potion.getAllEffects()) {
					added |= addKey(player, ThaumcraftRebornApi.id(
							"scan/" + scanPath(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()))
					));
				}
			}
			return added ? ScanResult.SCANNED : ScanResult.NOT_SCANNABLE;
		}

		private ScanResult grantPotionKeys(ServerPlayer player, LivingEntity entity, ScanResult base) {
			boolean added = false;
			for (MobEffectInstance effect : entity.getActiveEffects()) {
				added |= addKey(player, ThaumcraftRebornApi.id(
						"scan/" + scanPath(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()))
				));
			}
			return combine(base, added ? ScanResult.SCANNED : ScanResult.NOT_SCANNABLE);
		}

		private ScanResult combine(ScanResult first, ScanResult second) {
			if (first == ScanResult.SCANNED || second == ScanResult.SCANNED) {
				return ScanResult.SCANNED;
			}
			if (first == ScanResult.ALREADY_KNOWN || second == ScanResult.ALREADY_KNOWN) {
				return ScanResult.ALREADY_KNOWN;
			}
			return ScanResult.NOT_SCANNABLE;
		}
	}

	private static final class AspectLookupBridge {
		private AspectLookupBridge() {
		}

		private static io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList entityAspects(Entity entity) {
			return AspectLookup.get().getAspects(entity);
		}
	}
}
