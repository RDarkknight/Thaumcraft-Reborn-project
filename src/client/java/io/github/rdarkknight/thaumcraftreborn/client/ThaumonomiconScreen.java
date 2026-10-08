package io.github.rdarkknight.thaumcraftreborn.client;

import io.github.rdarkknight.thaumcraftreborn.api.ThaumcraftRebornApi;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.KnowledgeAccess;
import io.github.rdarkknight.thaumcraftreborn.api.knowledge.PlayerKnowledge;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchAccess;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchCategory;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchEntry;
import io.github.rdarkknight.thaumcraftreborn.api.research.ResearchStatus;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ThaumonomiconScreen extends Screen {
	private static final int ROW_HEIGHT = 14;
	private static final int TOP = 38;
	private List<Identifier> visibleCategories = List.of();
	private List<Identifier> visibleEntries = List.of();
	private Identifier selectedCategory;
	private Identifier selectedEntry;

	public ThaumonomiconScreen() {
		super(Component.translatable("item.thaumcraft_reborn.thaumonomicon_normal"));
	}

	@Override
	protected void init() {
		refreshCategories();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(18, 18, width - 18, height - 18, 0xE510141B);
		graphics.outline(18, 18, width - 18, height - 18, 0xFFB79B63);
		graphics.centeredText(font, title, width / 2, 23, 0xFFFFE6A4);
		graphics.text(font, Component.translatable("gui.thaumcraft_reborn.research.categories"), 28, 30, 0xFFD9C594);
		graphics.text(font, Component.translatable("gui.thaumcraft_reborn.research.entries"), width / 3, 30, 0xFFD9C594);
		graphics.text(font, Component.translatable("gui.thaumcraft_reborn.research.stages"), width * 2 / 3, 30, 0xFFD9C594);

		for (int index = 0; index < visibleCategories.size(); index++) {
			Identifier categoryId = visibleCategories.get(index);
			ResearchCategory category = ResearchAccess.get().category(categoryId, true).orElse(null);
			if (category == null) {
				continue;
			}
			int y = TOP + index * ROW_HEIGHT;
			drawRow(graphics, 26, width / 3 - 8, y, selectedCategory.equals(categoryId));
			graphics.text(font, categoryId.getPath(), 31, y + 3, 0xFFE6E0D2);
		}

		for (int index = 0; index < visibleEntries.size(); index++) {
			Identifier entryId = visibleEntries.get(index);
			ResearchEntry entry = ResearchAccess.get().entry(entryId, true).orElse(null);
			if (entry == null) {
				continue;
			}
			int x = width / 3 + 4;
			int y = TOP + index * ROW_HEIGHT;
			drawRow(graphics, x, width * 2 / 3 - 8, y, entryId.equals(selectedEntry));
			graphics.text(font, Component.translatable(entry.name()), x + 5, y + 3, 0xFFE6E0D2);
		}

		ResearchEntry selected = selectedEntry == null ? null : ResearchAccess.get().entry(selectedEntry, true).orElse(null);
		if (selected != null && Minecraft.getInstance().player != null) {
			PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(Minecraft.getInstance().player);
			int reached = Math.min(selected.stages().size(), Math.max(0, knowledge.getResearchStage(selectedEntry) + 1));
			int x = width * 2 / 3 + 8;
			int y = TOP;
			for (int stage = 0; stage < reached; stage++) {
				y = graphics.textWithWordWrap(
						font,
						Component.translatable(selected.stages().get(stage).text()),
						x,
						y,
						width / 3 - 38,
						0xFFF0E9D8
				) + 8;
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.x() < width / 3 && event.y() >= TOP) {
			int index = (int) ((event.y() - TOP) / ROW_HEIGHT);
			if (index >= 0 && index < visibleCategories.size()) {
				selectedCategory = visibleCategories.get(index);
				refreshEntries();
				return true;
			}
		} else if (event.x() < width * 2 / 3 && event.y() >= TOP) {
			int index = (int) ((event.y() - TOP) / ROW_HEIGHT);
			if (index >= 0 && index < visibleEntries.size()) {
				selectedEntry = visibleEntries.get(index);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void refreshCategories() {
		if (Minecraft.getInstance().player == null) {
			return;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(Minecraft.getInstance().player);
		Map<Identifier, ResearchCategory> categories = ResearchAccess.get().categories(true);
		visibleCategories = ResearchAccess.get().categoryOrder(true).stream()
				.filter(id -> {
					ResearchCategory category = categories.get(id);
					return category != null && category.researchKey().map(knowledge::isResearchKnown).orElse(true);
				})
				.toList();
		if (selectedCategory == null || !visibleCategories.contains(selectedCategory)) {
			selectedCategory = visibleCategories.stream().findFirst().orElse(null);
		}
		refreshEntries();
	}

	private void refreshEntries() {
		if (selectedCategory == null || Minecraft.getInstance().player == null) {
			visibleEntries = List.of();
			selectedEntry = null;
			return;
		}
		PlayerKnowledge knowledge = KnowledgeAccess.get().knowledge(Minecraft.getInstance().player);
			visibleEntries = ResearchAccess.get().entriesInCategory(selectedCategory, true).stream()
				.filter(id -> knowledge.getResearchStatus(id) != ResearchStatus.UNKNOWN)
				.toList();
		if (selectedEntry == null || !visibleEntries.contains(selectedEntry)) {
			selectedEntry = visibleEntries.stream().findFirst().orElse(null);
		}
	}

	private void drawRow(GuiGraphicsExtractor graphics, int left, int right, int y, boolean selected) {
		graphics.fill(left, y, right, y + ROW_HEIGHT - 1, selected ? 0xA34E422B : 0x7040434A);
	}
}
