package io.github.rdarkknight.thaumcraftreborn.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class ThaumcraftContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
	protected ThaumcraftContainerScreen(M menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	protected abstract Identifier backgroundTexture();

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blit(
				backgroundTexture(),
				leftPos,
				topPos,
				leftPos + imageWidth,
				topPos + imageHeight,
				0.0F,
				(float) imageWidth / textureWidth(),
				0.0F,
				(float) imageHeight / textureHeight()
		);
	}

	protected int textureWidth() {
		return 256;
	}

	protected int textureHeight() {
		return 256;
	}
}
