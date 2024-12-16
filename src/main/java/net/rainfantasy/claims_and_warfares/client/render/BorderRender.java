package net.rainfantasy.claims_and_warfares.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class BorderRender {
	
	private static final ResourceLocation BORDER_TEXTURE = new ResourceLocation("textures/misc/forcefield.png");
	
	
	public static void renderBorder(Vector2d minPos, Vector2d maxPos, Vector3d currentPos, double renderedYHeight, int borderColor, boolean visibleOutside) {
		double renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
		Vector2d currentPosPlanar = new Vector2d(currentPos.x, currentPos.z);
		if (isIn(minPos, maxPos, currentPosPlanar) || (visibleOutside && isCloseEnough(minPos, maxPos, currentPosPlanar, renderDistance))) {
			double alpha = 1 - (Math.abs(distanceToClosestBorder(minPos, maxPos, currentPosPlanar)) / renderDistance);
			alpha = Mth.clamp(Math.pow(alpha, 4), 0, 1);
			
			//double depth = Minecraft.getInstance().gameRenderer.getDepthFar();
			double depth = renderedYHeight / 2;
			
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
			RenderSystem.setShaderTexture(0, BORDER_TEXTURE);
			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			
			PoseStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushPose();
			RenderSystem.applyModelViewMatrix();
			
			RenderSystem.setShaderColor(ColorUtil.red(borderColor) / 255f, ColorUtil.green(borderColor) / 255f, ColorUtil.blue(borderColor) / 255f, (float) alpha);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			
			RenderSystem.polygonOffset(-3, -3);
			RenderSystem.enablePolygonOffset();
			
			RenderSystem.disableCull();
			
			float animationTime = System.currentTimeMillis() % 3000 / 3000f;
			//float uvOffset = (float) (depth - Mth.frac(currentPos.y));
			float uvOffset = (float) (depth - Mth.frac(currentPos.y));
			
			double maxVisibleX = Math.min(Mth.ceil(currentPos.x + renderDistance), maxPos.x);
			double maxVisibleZ = Math.min(Mth.ceil(currentPos.z + renderDistance), maxPos.y);
			double minVisibleX = Math.max(Mth.floor(currentPos.x - renderDistance), minPos.x);
			double minVisibleZ = Math.max(Mth.floor(currentPos.z - renderDistance), minPos.y);
			
			
			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			
			//Positive X
			if (currentPos.x > maxPos.x - renderDistance || (visibleOutside && currentPos.x < minPos.x + renderDistance)) {
				for (double curZ = minVisibleZ; curZ < maxVisibleZ; curZ += 0.5) {
					float zOffset = (float) (Math.min(maxVisibleZ - curZ, 1) * 0.5f);
					bufferBuilder.vertex(maxPos.x - currentPos.x, -depth, curZ - currentPos.z).uv(animationTime, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(maxPos.x - currentPos.x, -depth, curZ + zOffset - currentPos.z).uv(animationTime - zOffset, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(maxPos.x - currentPos.x, depth, curZ + zOffset - currentPos.z).uv(animationTime - zOffset, animationTime).endVertex();
					bufferBuilder.vertex(maxPos.x - currentPos.x, depth, curZ - currentPos.z).uv(animationTime, animationTime).endVertex();
					//curZ++;
				}
			}
			
			//Negative X
			if (currentPos.x < minPos.x + renderDistance || (visibleOutside && currentPos.x > maxPos.x - renderDistance)) {
				for (double curZ = minVisibleZ; curZ < maxVisibleZ; curZ += 0.5) {
					float zOffset = (float) (Math.min(maxVisibleZ - curZ, 1) * 0.5f);
					bufferBuilder.vertex(minPos.x - currentPos.x, -depth, curZ - currentPos.z).uv(animationTime, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(minPos.x - currentPos.x, -depth, curZ + zOffset - currentPos.z).uv(animationTime + zOffset, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(minPos.x - currentPos.x, depth, curZ + zOffset - currentPos.z).uv(animationTime + zOffset, animationTime).endVertex();
					bufferBuilder.vertex(minPos.x - currentPos.x, depth, curZ - currentPos.z).uv(animationTime, animationTime).endVertex();
					//curZ++;
				}
			}
			
			//Positive Z
			if (currentPos.z > maxPos.y - renderDistance || (visibleOutside && currentPos.z < minPos.y + renderDistance)) {
				for (double curX = minVisibleX; curX < maxVisibleX; curX += 0.5) {
					float xOffset = (float) (Math.min(maxVisibleX - curX, 1) * 0.5f);
					bufferBuilder.vertex(curX - currentPos.x, -depth, maxPos.y - currentPos.z).uv(animationTime, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(curX + xOffset - currentPos.x, -depth, maxPos.y - currentPos.z).uv(animationTime + xOffset, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(curX + xOffset - currentPos.x, depth, maxPos.y - currentPos.z).uv(animationTime + xOffset, animationTime).endVertex();
					bufferBuilder.vertex(curX - currentPos.x, depth, maxPos.y - currentPos.z).uv(animationTime, animationTime).endVertex();
					//curX++;
				}
			}
			
			//Negative Z
			if (currentPos.z < minPos.y + renderDistance || (visibleOutside && currentPos.z > maxPos.y - renderDistance)) {
				for (double curX = minVisibleX; curX < maxVisibleX; curX += 0.5) {
					float xOffset = (float) (Math.min(maxVisibleX - curX, 1) * 0.5f);
					bufferBuilder.vertex(curX - currentPos.x, -depth, minPos.y - currentPos.z).uv(animationTime, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(curX + xOffset - currentPos.x, -depth, minPos.y - currentPos.z).uv(animationTime - xOffset, animationTime + uvOffset).endVertex();
					bufferBuilder.vertex(curX + xOffset - currentPos.x, depth, minPos.y - currentPos.z).uv(animationTime - xOffset, animationTime).endVertex();
					bufferBuilder.vertex(curX - currentPos.x, depth, minPos.y - currentPos.z).uv(animationTime, animationTime).endVertex();
					//curX++;
				}
			}
			
			BufferUploader.drawWithShader(bufferBuilder.end());
			RenderSystem.enableCull();
			RenderSystem.polygonOffset(0, 0);
			RenderSystem.disablePolygonOffset();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			poseStack.popPose();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setShaderColor(1, 1, 1, 1);
			RenderSystem.depthMask(true);
			
		}
	}
	
	private static boolean isIn(Vector2d minPos, Vector2d maxPos, Vector2d currentPos) {
		return currentPos.x >= minPos.x && currentPos.x <= maxPos.x && currentPos.y >= minPos.y && currentPos.y <= maxPos.y;
	}
	
	private static boolean isCloseEnough(Vector2d minPos, Vector2d maxPos, Vector2d currentPos, double distance) {
		return currentPos.x >= minPos.x - distance && currentPos.x <= maxPos.x + distance && currentPos.y >= minPos.y - distance && currentPos.y <= maxPos.y + distance;
	}
	
	private static double distanceToClosestBorder(Vector2d minPos, Vector2d maxPos, Vector2d currentPos) {
		double xDist = Math.min(currentPos.x - minPos.x, maxPos.x - currentPos.x);
		double yDist = Math.min(currentPos.y - minPos.y, maxPos.y - currentPos.y);
		return Math.min(xDist, yDist);
	}
	
}
