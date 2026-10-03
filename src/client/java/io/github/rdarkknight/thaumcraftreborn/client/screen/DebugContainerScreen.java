package io.github.rdarkknight.thaumcraftreborn.client.screen;

import io.github.rdarkknight.thaumcraftreborn.ThaumcraftReborn;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class DebugContainerScreen extends ThaumcraftContainerScreen<DebugContainerMenu> {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

	public DebugContainerScreen(DebugContainerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected Identifier backgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		graphics.text(font, Component.literal("Ticks: " + menu.ticks()), 110, 6, 0xFF404040);
	}
}
