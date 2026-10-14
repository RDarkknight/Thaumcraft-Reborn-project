package io.github.rdarkknight.thaumcraftreborn.client.screen;

import io.github.rdarkknight.thaumcraftreborn.content.workbench.ArcaneWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ArcaneWorkbenchScreen extends ThaumcraftContainerScreen<ArcaneWorkbenchMenu> {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

	public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 220, 230);
		inventoryLabelY = 105;
	}

	@Override
	protected Identifier backgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int x = leftPos;
		int y = topPos;
		graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF211D28);
		graphics.fill(x + 6, y + 6, x + imageWidth - 6, y + 89, 0xFF302A38);
		graphics.fill(x + 6, y + 98, x + imageWidth - 6, y + imageHeight - 6, 0xFF302A38);
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				drawSlot(graphics, x + 16 + column * 18, y + 17 + row * 18);
				drawSlot(graphics, x + 90 + column * 18, y + 17 + row * 18);
			}
		}
		drawSlot(graphics, x + 177, y + 35);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		graphics.text(font, Component.translatable("container.thaumcraft_reborn.arcane_crystals"),
				leftPos + 90, topPos + 6, 0xFFE4D6A7);
	}

	private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 18, y + 18, 0xFF17141B);
		graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF514A58);
	}
}
