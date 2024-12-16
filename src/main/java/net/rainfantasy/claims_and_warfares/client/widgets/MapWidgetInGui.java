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

public class MapWidgetInGui implements Renderable, GuiEventListener, NarratableEntry {
	
	MapTexture texture = new MapTexture();
	int topX;
	int leftY;
	
	public MapWidgetInGui(int x, int y) {
		this.topX = x;
		this.leftY = y;
	}
	
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		this.texture.draw(guiGraphics, this.topX, this.leftY);
		poseStack.popPose();
	}
	
	public Vector2i getRelativeMousePos(int mouseX, int mouseY) {
		return new Vector2i(mouseX - this.topX, mouseY - this.leftY);
	}
	
	public Vector2i worldCoordToRelativeCoord(int x, int y) {
		return new Vector2i(x, y).sub(CAWClientGUIManager.mapPos).add(this.topX, this.leftY);
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
