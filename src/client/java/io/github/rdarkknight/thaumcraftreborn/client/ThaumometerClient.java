package io.github.rdarkknight.thaumcraftreborn.client;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectList;
import io.github.rdarkknight.thaumcraftreborn.api.aspect.AspectLookup;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchScreenAccess;
import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.network.AuraSyncPayload;
import io.github.rdarkknight.thaumcraftreborn.core.network.ScanSlotPayload;
import io.github.rdarkknight.thaumcraftreborn.client.mixin.ContainerScreenAccessor;
import io.github.rdarkknight.thaumcraftreborn.systems.RevealingEquipment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ThaumometerClient {
	private static AuraSyncPayload aura;

	private ThaumometerClient() {
	}

	public static void init() {
		ResearchScreenAccess.install(() -> Minecraft.getInstance().gui.setScreen(new ThaumonomiconScreen()));
		ClientPlayNetworking.registerGlobalReceiver(AuraSyncPayload.TYPE, (payload, context) ->
				context.client().execute(() -> aura = payload));
		ClientTickEvents.END_CLIENT_TICK.register(ThaumometerClient::scanHoveredSlot);
		HudElementRegistry.addLast(ThaumcraftRebornApi.id("thaumometer_hud"), ThaumometerClient::renderHud);
	}

	private static void scanHoveredSlot(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || player.tickCount % 5 != 0
				|| !(player.getMainHandItem().is(ThaumcraftContent.THAUMOMETER)
						|| player.getOffhandItem().is(ThaumcraftContent.THAUMOMETER))
				|| !(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
			return;
		}
		var slot = ((ContainerScreenAccessor) screen).thaumcraft_reborn$getHoveredSlot();
		if (slot == null || slot.getItem().isEmpty()) {
			return;
		}
		int slotId = screen.getMenu().slots.indexOf(slot);
		if (slotId >= 0) {
			ClientPlayNetworking.send(new ScanSlotPayload(screen.getMenu().containerId, slotId));
		}
	}

	private static void renderHud(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		boolean enabled = RevealingEquipment.isRevealing(player)
				|| player.getMainHandItem().is(ThaumcraftContent.THAUMOMETER)
				|| player.getOffhandItem().is(ThaumcraftContent.THAUMOMETER);
		boolean thaumometerHeld = player.getMainHandItem().is(ThaumcraftContent.THAUMOMETER)
				|| player.getOffhandItem().is(ThaumcraftContent.THAUMOMETER);
		if (!enabled) {
			return;
		}
		if (RevealingEquipment.isRevealing(player)) {
			graphics.text(client.font, Component.translatable("hud.thaumcraft_reborn.revealing"), 8, 27, 0xFFFFE6A4);
		}
		if (aura != null && thaumometerHeld) {
			drawAura(graphics, aura);
		}
		AspectList aspects = lookedAtAspects(client);
		int y = 34;
		for (var aspect : aspects.sortedByAmount().stream().limit(6).toList()) {
			graphics.text(client.font, aspect.tag() + " " + aspects.amount(aspect), 8, y, aspect.color() | 0xFF000000);
			y += 12;
		}
		if (client.gui.screen() instanceof AbstractContainerScreen<?> screen) {
			var slot = ((ContainerScreenAccessor) screen).thaumcraft_reborn$getHoveredSlot();
			if (slot != null && !slot.getItem().isEmpty()) {
				drawAspects(graphics, client.font, AspectLookup.get().getAspects(player.level(), slot.getItem()), 8, y);
			}
		}
	}

	private static AspectList lookedAtAspects(Minecraft client) {
		if (client.hitResult instanceof EntityHitResult entityHit) {
			Entity entity = entityHit.getEntity();
			return AspectLookup.get().getAspects(entity);
		}
		if (client.hitResult instanceof BlockHitResult blockHit && client.level != null) {
			Block block = client.level.getBlockState(blockHit.getBlockPos()).getBlock();
			return AspectLookup.get().getAspects(client.level, new ItemStack(block.asItem()));
		}
		if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY
				&& client.hitResult instanceof EntityHitResult entityHit) {
			return AspectLookup.get().getAspects(entityHit.getEntity());
		}
		return AspectList.EMPTY;
	}

	private static void drawAura(GuiGraphicsExtractor graphics, AuraSyncPayload data) {
		Minecraft client = Minecraft.getInstance();
		int left = 8;
		int top = 8;
		int width = 92;
		float vis = Math.clamp(data.vis() / 525.0F, 0.0F, 1.0F);
		float flux = Math.clamp(data.flux() / 525.0F, 0.0F, 1.0F);
		float base = Math.clamp(data.base() / 525.0F, 0.0F, 1.0F);
		float total = vis + flux;
		if (total > 1.0F) {
			vis /= total;
			flux /= total;
		}
		graphics.fill(left, top, left + width, top + 8, 0xA0000000);
		graphics.fill(left, top, left + Math.round(width * vis), top + 8, 0xCCB96AE8);
		graphics.fill(left, top + 9, left + width, top + 17, 0xA0000000);
		graphics.fill(left, top + 9, left + Math.round(width * flux), top + 17, 0xCC67338B);
		graphics.fill(left + Math.round(width * base), top - 1, left + Math.round(width * base) + 2, top + 18, 0xFFFFE09B);
		if (client.player != null && client.player.isCrouching()) {
			graphics.text(client.font, Component.translatable("hud.thaumcraft_reborn.vis").getString()
					+ " " + (int) data.vis() + " / "
					+ Component.translatable("hud.thaumcraft_reborn.flux").getString() + " " + (int) data.flux(),
					left, top + 20, 0xFFEAE3F2);
		}
	}

	private static void drawAspects(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, AspectList aspects, int x, int y) {
		for (var aspect : aspects.sortedByAmount().stream().limit(6).toList()) {
			graphics.text(font, aspect.tag() + " " + aspects.amount(aspect), x, y, aspect.color() | 0xFF000000);
			y += 12;
		}
	}
}
