package io.github.rdarkknight.thaumcraftreborn.client.screen;

import io.github.rdarkknight.thaumcraftreborn.content.research.ResearchTableMenu;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.ResearchTableData;
import io.github.rdarkknight.thaumcraftreborn.api.research.theorycraft.TheorycraftAccess;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ResearchTableScreen extends ThaumcraftContainerScreen<ResearchTableMenu> {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

	public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 230, 235);
		inventoryLabelY = 126;
	}

	@Override
	protected void init() {
		super.init();
		addMenuButton(Component.translatable("gui.thaumcraft_reborn.theory.start"), ResearchTableMenu.START_SESSION, 105, 18);
		addMenuButton(Component.translatable("gui.thaumcraft_reborn.theory.draw_two"), ResearchTableMenu.DRAW_TWO, 105, 40);
		addMenuButton(Component.translatable("gui.thaumcraft_reborn.theory.draw_three"), ResearchTableMenu.DRAW_THREE, 105, 62);
		for (int index = 0; index < 3; index++) {
			addMenuButton(Component.translatable("gui.thaumcraft_reborn.theory.pick", index + 1),
					ResearchTableMenu.PICK_CARD_BASE + index, 172, 18 + index * 22);
		}
	}

	private void addMenuButton(Component label, int buttonId, int x, int y) {
		addRenderableWidget(Button.builder(label, button -> {
			if (minecraft != null && minecraft.gameMode != null) {
				minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
			}
		}).bounds(leftPos + x, topPos + y, 52, 18).build());
	}

	@Override
	protected Identifier backgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF211D28);
		graphics.fill(leftPos + 6, topPos + 6, leftPos + imageWidth - 6, topPos + 116, 0xFF302A38);
		graphics.fill(leftPos + 6, topPos + 120, leftPos + imageWidth - 6, topPos + imageHeight - 6, 0xFF302A38);
		graphics.fill(leftPos + 10, topPos + 82, leftPos + 96, topPos + 118, 0xFF211D28);
		drawSlot(graphics, leftPos + 25, topPos + 23);
		drawSlot(graphics, leftPos + 61, topPos + 23);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		ResearchTableData data = menu.data();
		graphics.text(font, Component.translatable("gui.thaumcraft_reborn.theory.inspiration",
				data.inspiration(), data.inspirationStart()), 14, 86, 0xFFE4D6A7);
		int row = 0;
		for (var category : java.util.List.of("basics", "alchemy")) {
			var total = data.categoryTotals().getOrDefault(Identifier.fromNamespaceAndPath("thaumcraft_reborn", category), 0);
			graphics.text(font, Component.translatable("gui.thaumcraft_reborn.theory.total",
					category.toUpperCase(java.util.Locale.ROOT), total),
					12, 98 + row * 10, 0xFFE4D6A7);
			row++;
		}
		for (int index = 0; index < Math.min(3, data.offeredCards().size()); index++) {
			var offer = data.offeredCards().get(index);
			var card = TheorycraftAccess.get().card(offer.card());
			if (card.isPresent()) {
				Component name = card.get().name(offer);
				String visible = font.plainSubstrByWidth(name.getString(), 112);
				graphics.text(font, visible, 105, 88 + index * 10, 0xFFFFFFFF);
			}
		}
	}

	private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 18, y + 18, 0xFF17141B);
		graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF514A58);
	}
}
