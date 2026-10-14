package io.github.rdarkknight.thaumcraftreborn.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class CrucibleBlockEntityRenderState extends BlockEntityRenderState {
	public final ItemStackRenderState water = new ItemStackRenderState();
	public float level;
}
