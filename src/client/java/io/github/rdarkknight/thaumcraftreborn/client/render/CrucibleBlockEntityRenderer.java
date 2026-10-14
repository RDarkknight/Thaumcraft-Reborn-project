package io.github.rdarkknight.thaumcraftreborn.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.rdarkknight.thaumcraftreborn.content.crucible.CrucibleBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class CrucibleBlockEntityRenderer
		implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleBlockEntityRenderState> {
	private final BlockEntityRendererProvider.Context context;

	public CrucibleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public CrucibleBlockEntityRenderState createRenderState() {
		return new CrucibleBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(CrucibleBlockEntity blockEntity, CrucibleBlockEntityRenderState state,
			float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.level = blockEntity.waterMb() / (float) CrucibleBlockEntity.CAPACITY;
		context.itemModelResolver().updateForTopItem(state.water,
				new ItemStack(Items.BLUE_ICE), ItemDisplayContext.FIXED,
				blockEntity.getLevel(), null, 0);
	}

	@Override
	public void submit(CrucibleBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState cameraState) {
		if (state.level <= 0.0F) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.5, 0.22 + state.level * 0.76, 0.5);
		poseStack.scale(0.72F, 0.035F, 0.72F);
		state.water.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, 0, 0);
		poseStack.popPose();
	}
}
