package net.rainfantasy.claims_and_warfares.client.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.widgets.MapWidget;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class ClaimMapScreen extends Screen {
	
	public static final ResourceLocation MAP_WIDGETS = new ResourceLocation("textures/map/map_icons.png");
	
	private MapWidget mapWidget;
	
	public ClaimMapScreen() {
		super(Component.literal("Claim Map Viewer"));
		//super.minecraft = Minecraft.getInstance();
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		PoseStack poseStack = graphics.pose();
		
		
		poseStack.pushPose();
		RenderSystem.enableDepthTest();
		super.render(graphics, 0, 0, delta);
		RenderSystem.disableDepthTest();
		poseStack.popPose();
		
		/* code to draw mouse cursor
		 *
		 * poseStack.pushPose();
		 * poseStack.translate(0, 0, 5);
		 * graphics.blit(GUI_ICONS, mouseX - 7, mouseY - 7,
		 * 0, 0, 15, 15);
		 * poseStack.popPose();
		 *
		 */
		Entity clientPlayer = Minecraft.getInstance().getCameraEntity();
		if(clientPlayer != null) {
			Vec3 pos = clientPlayer.position();
			int xLeftCorner = (this.width - CAWClientGUIManager.mapInfo.length) / 2;
			int yTopCorner = (this.height - CAWClientGUIManager.mapInfo[0].length) / 2;
			int x = (int) (pos.x - CAWClientGUIManager.mapPos.x + xLeftCorner);
			int y = (int) (pos.z - CAWClientGUIManager.mapPos.y + yTopCorner);
			poseStack.pushPose();
			poseStack.translate(x, y, 0);
			poseStack.mulPose(Axis.ZP.rotationDegrees(((clientPlayer.getYRot() - 180) % 360f)));
			poseStack.scale(0.5f, 0.5f, 0.5f);
			poseStack.translate(-0.125F, 0.125F, 0.0F);
			graphics.blit(MAP_WIDGETS, -8, -8, 0, 0, 16, 16);
			poseStack.popPose();
		}
		
		if (mapWidget.isMouseInBounds(mouseX, mouseY)) {
			Vector2i relativePos = mapWidget.getRelativeMousePos(mouseX, mouseY);
			Vector2i worldPos = new Vector2i(relativePos.x + CAWClientGUIManager.mapPos.x, relativePos.y + CAWClientGUIManager.mapPos.y);
			Vector2i chunkPos = new Vector2i(relativePos.x >> 4, relativePos.y >> 4);
			Vector2i chunkInChunk = new Vector2i(relativePos.x & 15, relativePos.y & 15);
			MutableComponent comp = this.addTooltip(worldPos, chunkPos, chunkInChunk);
			this.setTooltipForNextRenderPass(comp);
		}
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.mapWidget = this.addRenderableWidget(new MapWidget(this.width, this.height, 0, 0));
		this.setFocused(mapWidget);
	}
	
	@Override
	public void tick() {
		if (mapWidget != null) {
			mapWidget.tick();
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@SuppressWarnings("DuplicatedCode")
	public MutableComponent addTooltip(Vector2i posXZ, Vector2i posXZChunk, Vector2i posXZInChunk) {
		MutableComponent result = Component.literal("");
		result.append("X: " + (posXZ.x) + " Z: " + (posXZ.y));
		if (CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y] != null) {
			result.append("\n").append(CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y].Name);
		}
		return result;
	}
}
