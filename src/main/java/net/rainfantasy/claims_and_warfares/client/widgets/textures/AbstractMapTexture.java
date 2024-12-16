package net.rainfantasy.claims_and_warfares.client.widgets.textures;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix4f;

public abstract class AbstractMapTexture extends AbstractTexture {
	
	int imageWidth;
	int imageHeight;
	
	public void draw(GuiGraphics graphics, int x, int y) {
		if (this.getId() != -1) {
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, this.getId());
			Matrix4f matrix4f = graphics.pose().last().pose();
			bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex(matrix4f, (float) x, (float) (y + this.imageHeight), 0.0F).uv(0.0F, 1.0F).color(-1).endVertex();
			bufferbuilder.vertex(matrix4f, (float) (x + this.imageWidth), (float) (y + this.imageHeight), 0.0F).uv(1.0F, 1.0F).color(-1).endVertex();
			bufferbuilder.vertex(matrix4f, (float) (x + this.imageWidth), (float) y, 0.0F).uv(1.0F, 0.0F).color(-1).endVertex();
			bufferbuilder.vertex(matrix4f, (float) x, (float) y, 0.0F).uv(0.0F, 0.0F).color(-1).endVertex();
			tesselator.end();
		}
	}
	
	abstract NativeImage getImage();
	
	public void load() {
		this.doLoad(getImage());
	}
	
	private void doLoad(NativeImage image) {
		TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), image.getHeight());
		image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, true);
	}
}
