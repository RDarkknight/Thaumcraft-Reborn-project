package io.github.rdarkknight.thaumcraftreborn.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class TestRenderBlockEntityRenderState extends BlockEntityRenderState {
	public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
	public float rotation;
}
