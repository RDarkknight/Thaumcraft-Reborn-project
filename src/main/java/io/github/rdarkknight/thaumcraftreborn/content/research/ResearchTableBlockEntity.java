package io.github.rdarkknight.thaumcraftreborn.content.research;

import io.github.rdarkknight.thaumcraftreborn.content.ThaumcraftContent;
import io.github.rdarkknight.thaumcraftreborn.core.block.ThaumcraftContainerBlockEntity;
import io.github.rdarkknight.thaumcraftreborn.systems.research.theorycraft.ResearchTableData;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ResearchTableBlockEntity extends ThaumcraftContainerBlockEntity implements ExtendedMenuProvider<BlockPos> {
	private ResearchTableData data = new ResearchTableData();

	public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
		super(ThaumcraftContent.RESEARCH_TABLE_BLOCK_ENTITY, pos, state, 2);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		data = input.read("theorycraft", ResearchTableData.CODEC).orElseGet(ResearchTableData::new);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("theorycraft", ResearchTableData.CODEC, data);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.thaumcraft_reborn.research_table");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new ResearchTableMenu(containerId, inventory, getBlockPos());
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return getBlockPos();
	}

	public ResearchTableData data() {
		return data;
	}
}
