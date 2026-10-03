package io.github.rdarkknight.thaumcraftreborn.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.rdarkknight.thaumcraftreborn.content.debug.DebugContent;
import io.github.rdarkknight.thaumcraftreborn.content.debug.TestRenderBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class TestRenderBlockEntityRenderer implements BlockEntityRenderer<TestRenderBlockEntity, TestRenderBlockEntityRenderState> {
	private final BlockEntityRendererProvider.Context context;

	public TestRenderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public TestRenderBlockEntityRenderState createRenderState() {
		return new TestRenderBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(TestRenderBlockEntity blockEntity, TestRenderBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
		state.rotation = (blockEntity.getLevel().getGameTime() + tickProgress) * 2.0f;
		this.context.itemModelResolver().updateForTopItem(
				state.itemRenderState,
				new ItemStack(DebugContent.TEST_PROBE),
				ItemDisplayContext.FIXED,
				blockEntity.getLevel(),
				null,
				0
		);
	}

	@Override
	public void submit(TestRenderBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		poseStack.pushPose();
		poseStack.translate(0.5, 1.4, 0.5);
		poseStack.mulPose(new Matrix4f().rotateY((float) Math.toRadians(90.0f + state.rotation)));
		poseStack.scale(0.65f, 0.65f, 0.65f);
		state.itemRenderState.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, 0, 0);
		poseStack.popPose();
	}
}
