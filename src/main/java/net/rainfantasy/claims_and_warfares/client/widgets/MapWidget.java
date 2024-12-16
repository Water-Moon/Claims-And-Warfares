package net.rainfantasy.claims_and_warfares.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.widgets.textures.MapTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class MapWidget implements Renderable, GuiEventListener, NarratableEntry {
	
	MapTexture texture = new MapTexture();
	int parentWidth;
	int parentHeight;
	int offsetX;
	int offsetY;
	
	public MapWidget(int parentWidth, int parentHeight, int offsetX, int offsetY) {
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public Vector2i getTopLeftCorner() {
		int thisWidth = CAWClientGUIManager.mapInfo.length;
		int thisHeight = CAWClientGUIManager.mapInfo[0].length;
		int centerX = offsetX + parentWidth / 2;
		int centerY = offsetY + parentHeight / 2;
		int topLeftX = centerX - thisWidth / 2;
		int topLeftY = centerY - thisHeight / 2;
		return new Vector2i(topLeftX, topLeftY);
	}
	
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		Vector2i topLeft = getTopLeftCorner();
		this.texture.draw(guiGraphics, topLeft.x(), topLeft.y());
		poseStack.popPose();
	}
	
	public Vector2i getRelativeMousePos(int mouseX, int mouseY) {
		Vector2i topLeft = getTopLeftCorner();
		return new Vector2i(mouseX - topLeft.x, mouseY - topLeft.y);
	}
	
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		Vector2i relativePos = getRelativeMousePos(mouseX, mouseY);
		return relativePos.x >= 0 && relativePos.x < CAWClientGUIManager.mapInfo.length && relativePos.y >= 0 && relativePos.y < CAWClientGUIManager.mapInfo[0].length;
	}
	
	@Override
	public @NotNull NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}
	
	@Override
	public void setFocused(boolean b) {
	
	}
	
	@Override
	public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
	
	}
	
	@Override
	public boolean isFocused() {
		return false;
	}
	
	public void tick() {
		this.texture.update();
	}
	
}
