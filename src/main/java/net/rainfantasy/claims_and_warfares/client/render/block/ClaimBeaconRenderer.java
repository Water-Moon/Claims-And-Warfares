package net.rainfantasy.claims_and_warfares.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity.BeaconBeamSection;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconHelper;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ClaimBeaconRenderer implements BlockEntityRenderer<ClaimBeaconBlockEntity> {
	
	public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
	
	private static final float[] COLOR_ERRORED = new float[]{1f, 0f, 0f};
	private static final float[] COLOR_STARTING = new float[]{1f, 1f, 0f};
	private static final float[] COLOR_STOPPING = new float[]{1f, 0.5f, 0f};
	private static final float[] COLOR_EDITING = new float[]{0f, 0.5f, 1f};
	private static final float[] COLOR_UNSTABLE = new float[]{1f, 0f, 1f};
	private static final float[] COLOR_FAILED = new float[]{0f, 0f, 0f};
	
	Context context;
	
	public ClaimBeaconRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}
	
	@Override
	public void render(@NotNull ClaimBeaconBlockEntity block, float delta, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
		boolean showExtendedInfo = this.shouldShowExtendedInfo(block);
		if (block.getStatus() != ClaimBeaconBlockEntity.STATUS_OFF) {
			this.renderBeam(showExtendedInfo ? 2 : 0, block, poseStack, buffer, delta);
		}
		if (showExtendedInfo) {
			this.renderExtendedInfo(block, delta, poseStack, buffer, packedLight, packedOverlay);
		}
	}
	
	private void renderExtendedInfo(@NotNull ClaimBeaconBlockEntity block, float delta, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Component content = Component.translatable("caw.label.beacon.owner", block.getOwnerName());
		Component content2 = Component.translatable("caw.label.beacon.faction", block.getOwningFactionName());
		renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content, 0);
		renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content2, 1);
		if (block.getHackProgress() > 0) {
			Component content3 = Component.translatable("caw.label.beacon.hacking", block.getHackProgress(), block.getMaxHackProgress());
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content3, 2);
		} else if (BeaconHelper.isUnstableState(block.getStatus()) && block.getUnstableTime() > 0) {
			Component content3 = Component.translatable("caw.label.beacon.deactivate_in", block.getEventTime().format());
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content3, 2);
		} else if (block.getStatus() == ClaimBeaconBlockEntity.STATUS_UNSTABLE_REPAIRING) {
			int time = block.getTransientTime(ClaimBeaconBlockEntity.STATUS_UNSTABLE_REPAIRING);
			Component content3 = Component.translatable("caw.label.beacon.repairing", time - block.getTimer(), time);
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content3, 2);
		}
		if (block.getStatus() == ClaimBeaconBlockEntity.STATUS_ERRORED_OFF || block.getStatus() == ClaimBeaconBlockEntity.STATUS_ERRORED_STOPPING) {
			Component content3 = Component.translatable("caw.label.beacon.errored");
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content3, 2);
			Component content4 = getErrorCode(block);
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content4, 3);
		}
		if (block.getStatus() == ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION) {
			Component content4 = Component.translatable("caw.label.beacon.contested");
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content4, 3);
		} else if (BeaconHelper.isUnstableState(block.getStatus())) {
			Component content4 = Component.translatable("caw.label.beacon.unstable");
			renderExtendedInfoLine(block, delta, poseStack, buffer, packedLight, packedOverlay, content4, 3);
		}
	}
	
	private static @NotNull Component getErrorCode(@NotNull ClaimBeaconBlockEntity block) {
		Component content3 = Component.literal("Unknown Error");
		switch (block.getErrorCode()) {
			case ClaimBeaconBlockEntity.ERROR_NO_FUEL ->
			content3 = Component.translatable("caw.label.beacon.error.no_fuel");
			case ClaimBeaconBlockEntity.ERROR_NO_OWNER ->
			content3 = Component.translatable("caw.label.beacon.error.no_owner");
			case ClaimBeaconBlockEntity.ERROR_CLAIM_CONFLICT ->
			content3 = Component.translatable("caw.label.beacon.error.claim_conflict");
			case ClaimBeaconBlockEntity.ERROR_OBSTRUCTED ->
			content3 = Component.translatable("caw.label.beacon.error.obstructed");
			case ClaimBeaconBlockEntity.ERROR_UPGRADE_CHANGED ->
			content3 = Component.translatable("caw.label.beacon.error.upgrade_changed");
		}
		return content3;
	}
	
	private void renderExtendedInfoLine(ClaimBeaconBlockEntity block, float delta, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Component line, int yOffset) {
		poseStack.pushPose();
		poseStack.translate(0.5, 1.25 + yOffset * 0.25, 0.5);
		poseStack.mulPose(context.getEntityRenderer().cameraOrientation());
		poseStack.scale(-0.025f, -0.025f, 0.025f);
		Matrix4f matrix4f = poseStack.last().pose();
		Font font = context.getFont();
		int backgroundColor = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.5f) * 255f) << 24;
		font.drawInBatch(
		line, ((float) -font.width(line) / 2), 0, ColorUtil.combine(200, 200, 200, 200), false, matrix4f, buffer, DisplayMode.NORMAL, backgroundColor, packedLight
		);
		poseStack.popPose();
	}
	
	private void renderBeam(int initialYOffset, ClaimBeaconBlockEntity block, PoseStack poseStack, MultiBufferSource buffer, float delta) {
		assert block.getLevel() != null;
		
		long time = block.getLevel().getGameTime();
		float[] color = ColorUtil.toFloatColor(block.getColor());
		float[] color1 = color.clone();
		switch (block.getStatus()) {
			case ClaimBeaconBlockEntity.STATUS_STARTING -> color1 = COLOR_STARTING;
			case ClaimBeaconBlockEntity.STATUS_STOPPING, ClaimBeaconBlockEntity.STATUS_ERRORED_STOPPING ->
			color1 = COLOR_STOPPING;
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_RUNNING -> color1 = COLOR_UNSTABLE;
			case ClaimBeaconBlockEntity.STATUS_ERRORED_OFF -> color1 = COLOR_ERRORED;
			case ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION,
			     ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION -> color1 = COLOR_EDITING;
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_REPAIRING -> color1 = COLOR_FAILED;
		}
		List<BeaconBeamSection> sections = this.generateSections(color1, color);
		int yPos = initialYOffset;
		
		for (int i = 0; i < sections.size(); ++i) {
			BeaconBlockEntity.BeaconBeamSection section = sections.get(i);
			BeaconRenderer.renderBeaconBeam(poseStack, buffer, BEAM_LOCATION, delta, 1.0F, time, yPos, (i == sections.size() - 1) ? 1024 : section.getHeight(), section.getColor(), 0.2F, 0.25F);
			yPos += section.getHeight();
		}
	}
	
	private List<BeaconBeamSection> generateSections(float[] color1, float[] color2) {
		ArrayList<BeaconBeamSection> sections = new ArrayList<>();
		int steps = 32;
		for (int i = 0; i < steps; i++) {
			sections.add(new BeaconBeamSection(new float[]{
			Mth.lerp(i / (float) steps, color1[0], color2[0]),
			Mth.lerp(i / (float) steps, color1[1], color2[1]),
			Mth.lerp(i / (float) steps, color1[2], color2[2])
			}));
		}
		BeaconBeamSection remaining = new BeaconBeamSection(color2);
		sections.add(remaining);
		return sections;
	}
	
	@Override
	public boolean shouldRenderOffScreen(@NotNull ClaimBeaconBlockEntity pBlockEntity) {
		return true;
	}
	
	@Override
	public int getViewDistance() {
		return 256;
	}
	
	@Override
	public boolean shouldRender(@NotNull ClaimBeaconBlockEntity pBlockEntity, @NotNull Vec3 pCameraPos) {
		return true;
	}
	
	protected boolean shouldShowExtendedInfo(ClaimBeaconBlockEntity block) {
		HitResult result = context.getEntityRenderer().camera.getEntity().pick(20, 0, false);
		return result.getLocation().distanceTo(block.getBlockPos().getCenter()) < 0.8661f;
	}
}