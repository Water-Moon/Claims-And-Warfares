package net.rainfantasy.claims_and_warfares.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.widgets.MapWidgetInGui;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.BeaconHackerMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconHackerOnOffPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconHackerSelectTargetPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BeaconHackerScreen extends AbstractContainerScreen<BeaconHackerMenu> {
	private static final ResourceLocation UI_TEXTURE = CAWConstants.rl("textures/gui/beacon_hack.png");
	
	int x, y;
	
	private boolean initialized = false;
	ArrayList<BlockPos> localBlockPos = new ArrayList<>();
	
	MapWidgetInGui mapWidget;
	Button[] buttons = new Button[9];
	Button startButton;
	Button stopButton;
	StringWidget selX;
	StringWidget selY;
	StringWidget selZ;
	StringWidget beaconOwnerName;
	StringWidget beaconFactionName;
	
	public BeaconHackerScreen(BeaconHackerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		CAWClientGUIManager.setClaimInfoSize(2, 2);
		this.imageHeight = 229;
		this.imageWidth = 255;
		this.inventoryLabelY = 135;
	}
	
	@Override
	protected void init() {
		super.init();
		CAWClientGUIManager.setClaimInfoSize(2, 2);
		x = (width - this.imageWidth)/2;
		y = (height - this.imageHeight)/2;
		
		this.mapWidget = this.addRenderableWidget(new MapWidgetInGui(x + 8, y + 17));
		for(int i = 0; i < 9; i++){
			int finalI = i;
			this.buttons[i] = this.addRenderableWidget(
				Button.builder(Component.empty(), (btn) -> {
					this.onButtonPress(finalI);
				}).pos(x + 178, y + 20 + i*21).size(72, 20).build()
			);
		}
		this.startButton = this.addRenderableWidget(
		Button.builder(Component.translatable("caw.gui.button.start"), (btn) -> {
			ChannelRegistry.sendToServer(new PTSBeaconHackerOnOffPacket(true));
		}).pos(x + 39, y + 113).size(51, 20).build()
		);
		this.stopButton = this.addRenderableWidget(
		Button.builder(Component.translatable("caw.gui.button.stop"), (btn) -> {
			ChannelRegistry.sendToServer(new PTSBeaconHackerOnOffPacket(false));
		}).pos(x + 96, y + 113).size(51, 20).build()
		);
		
		this.selX = new StringWidget(x + 109, y + 35, 52, 11, Component.literal("---"), this.font);
		this.selY = new StringWidget(x + 109, y + 48, 52, 11, Component.literal("---"), this.font);
		this.selZ = new StringWidget(x + 109, y + 61, 52, 11, Component.literal("---"), this.font);
		this.beaconOwnerName = new StringWidget(x + 109, y + 74, 52, 11, Component.literal("------"), this.font);
		this.beaconFactionName = new StringWidget(x + 109, y + 87, 52, 11, Component.literal("------"), this.font);
		this.addRenderableWidget(this.selX);
		this.addRenderableWidget(this.selY);
		this.addRenderableWidget(this.selZ);
		this.addRenderableWidget(this.beaconOwnerName);
		this.addRenderableWidget(this.beaconFactionName);
		this.initialized = true;
	}
	
	@Override
	protected void renderBg(GuiGraphics graphics, float v, int i, int i1) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		graphics.blit(UI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderBackground(graphics);
		super.render(graphics, pMouseX, pMouseY, pPartialTick);
		
		drawProgressBar(graphics, pMouseX, pMouseY);
		drawRemainingFuel(graphics, pMouseX, pMouseY);
		drawIcons(graphics, pMouseX, pMouseY);
		
		if(this.menu.block.isEnabled()){
			drawIndicatorLight(graphics, 96, 17, 2);
			if(this.menu.block.isTargetValid()){
				if(this.menu.block.getProgress() % 2 == 0){
					drawIndicatorLight(graphics, 107, 17, 2);
				}
			}
		}else{
			drawIndicatorLight(graphics, 96, 17, 0);
			if(this.menu.block.isTargetValid()){
				drawIndicatorLight(graphics, 107, 17, 1);
			}
		}
		
		renderTooltip(graphics, pMouseX, pMouseY);
	}
	
	@SuppressWarnings("DuplicatedCode")
	@Override
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		if(this.mapWidget == null){
			super.renderTooltip(graphics, mouseX, mouseY);
			return;
		}
		if(mapWidget.isMouseInBounds(mouseX, mouseY)){
			Vector2i relativePos = mapWidget.getRelativeMousePos(mouseX, mouseY);
			Vector2i worldPos = new Vector2i(relativePos.x + CAWClientGUIManager.mapPos.x, relativePos.y + CAWClientGUIManager.mapPos.y);
			Vector2i chunkPos = new Vector2i(relativePos.x >> 4, relativePos.y >> 4);
			Vector2i chunkInChunk = new Vector2i(relativePos.x & 15, relativePos.y & 15);
			graphics.renderTooltip(this.font,
			this.addTooltip(worldPos, chunkPos, chunkInChunk), Optional.empty(),
			mouseX, mouseY
			);
		} else {
			super.renderTooltip(graphics, mouseX, mouseY);
		}
	}
	
	@SuppressWarnings("DuplicatedCode")
	public List<Component> addTooltip(Vector2i posXZ, Vector2i posXZChunk, Vector2i posXZInChunk) {
		List<Component> result = new ArrayList<>();
		result.add(Component.literal("X: " + (posXZ.x) + " Z: " + (posXZ.y)));
		if (CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y] != null) {
			result.add(CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y].Name);
		}
		return result;
	}
	
	@Override
	protected void containerTick() {
		super.containerTick();
		if(!this.initialized) return;
		if(mapWidget != null) mapWidget.tick();
		copyPos();
		
		this.updateButtons();
		this.updateLabels();
	}
	
	private void copyPos(){
		Set<BlockPos> pos = this.menu.block.getKnownBeaconPos();
		localBlockPos.clear();
		localBlockPos.addAll(pos);
	}
	
	private void updateButtons(){
		int i = 0;
		for(BlockPos entry : localBlockPos){
			this.buttons[i].setMessage(Component.literal(entry.getX() + "," + entry.getZ()));
			this.buttons[i].active = true;
			i++;
		}
		while(i < 9){
			this.buttons[i].setMessage(Component.translatable("caw.gui.label.empty"));
			this.buttons[i].active = false;
			i++;
		}
		if(this.menu.block.isEnabled()){
			this.startButton.active = false;
			this.stopButton.active = true;
		} else {
			this.startButton.active = true;
			this.stopButton.active = false;
		}
	}
	
	private void drawProgressBar(GuiGraphics graphics, int mouseX, int mouseY){
		int barLength1 = (int)(((float)this.menu.block.getProgress() / BeaconHackerBlockEntity.MAX_PROGRESS) * 64);
		int barLength2 = (int)(((float)this.menu.block.getProgress2() / this.menu.block.getMaxProgress2()) * 64);
		graphics.blit(UI_TEXTURE, x + 97, y + 27, 48, 230, barLength1, 2);
		graphics.blit(UI_TEXTURE, x + 97, y + 31, 48, 232, barLength2, 2);
	}
	
	private void drawIcons(GuiGraphics graphics, int mouseX, int mouseY){
		if(mapWidget == null) return;
		BlockPos selfPos = this.menu.block.getBlockPos();
		Vector2i selfIconPos = mapWidget.worldCoordToRelativeCoord(selfPos.getX(), selfPos.getZ());
		drawIconInner(graphics, selfIconPos.x, selfIconPos.y, 48, 240, 7, 8, 4, 7);
		
		BlockPos selectedPos = this.menu.block.getTargetPos();
		if(this.menu.block.isTargetValid()) {
			Vector2i selectedIconPos = mapWidget.worldCoordToRelativeCoord(selectedPos.getX(), selectedPos.getZ());
			drawIconInner(graphics, selectedIconPos.x, selectedIconPos.y, 64, 240, 8, 8, 4, 4);
		}
		
		for(BlockPos pos : localBlockPos){
			if(pos.equals(selectedPos)) continue;
			Vector2i iconPos = mapWidget.worldCoordToRelativeCoord(pos.getX(), pos.getZ());
			drawIconInner(graphics, iconPos.x, iconPos.y, 59, 240, 4, 4, 2, 2);
		}
	}
	
	@SuppressWarnings("SameParameterValue")
	private void drawIconInner(GuiGraphics graphics, int tx, int ty, int u, int v, int uw, int vh, int cx, int cy){
		int posX = Mth.clamp(tx - cx, this.x + 6, this.x + 6 + 80);
		int posY = Mth.clamp(ty - cy, this.y + 15, this.y + 15 + 80);
		
		graphics.blit(UI_TEXTURE, posX, posY, u, v, uw, vh);
	}
	
	private void drawRemainingFuel(GuiGraphics graphics, int mouseX, int mouseY){
		int fuel = this.menu.block.getRemainingFuel();
		int maxFuel = this.menu.block.getMaxFuel();
		int barHeight = (int)(fuel / (float)maxFuel * 16);
		int yStart = 16 - barHeight;
		graphics.blit(UI_TEXTURE, x + 26, y + 117 + yStart, 44, 230 + yStart, 2, barHeight);
	}
	
	private void updateLabels(){
		BlockPos targetPos = this.menu.block.getTargetPos();
		String targetOwner = this.menu.block.getTargetOwner();
		String targetFaction = this.menu.block.getTargetFaction();
		
		this.selX.setMessage(Component.literal(String.valueOf(targetPos.getX())));
		this.selY.setMessage(Component.literal(String.valueOf(targetPos.getY())));
		this.selZ.setMessage(Component.literal(String.valueOf(targetPos.getZ())));
		
		this.beaconOwnerName.setMessage(Component.literal(targetOwner));
		this.beaconFactionName.setMessage(Component.literal(targetFaction));
	}
	
	private void onButtonPress(int i){
		if(i >= localBlockPos.size()) return;
		ChannelRegistry.sendToServer(new PTSBeaconHackerSelectTargetPacket(localBlockPos.get(i)));
	}
	
	
	int[][] indicatorPos = new int[][]{
	new int[]{21, 230},
	new int[]{28, 230},
	new int[]{35, 230},
	new int[]{21, 238},
	new int[]{28, 238},
	new int[]{35, 238},
	};
	protected void drawIndicatorLight(GuiGraphics graphics, int px, int py, int type){
		graphics.blit(UI_TEXTURE, x + px, y + py, indicatorPos[type][0], indicatorPos[type][1], 7, 7);
	}
}
