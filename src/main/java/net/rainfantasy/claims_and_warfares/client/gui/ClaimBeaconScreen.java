package net.rainfantasy.claims_and_warfares.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.widgets.MapWidgetInGui;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconHelper;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconPacketHandler;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconInstructionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"DuplicatedCode", "FieldCanBeLocal"})
public class ClaimBeaconScreen extends AbstractContainerScreen<ClaimBeaconMenu> {
	
	private static final ResourceLocation UI_TEXTURE = CAWConstants.rl("textures/gui/beacon.png");
	
	int x, y;
	
	private boolean initialized = false;
	
	private MapWidgetInGui mapWidget;
	private Button button1;
	private Button button2;
	private Button button3;
	
	private StringWidget remainFuelTime;
	private StringWidget size;
	private StringWidget ownerName;
	private StringWidget factionName;
	private StringWidget upgradeCount;
	
	int claimSize = 1;
	float[] progresses = new float[]{0f, 0f, 0f};
	
	public ClaimBeaconScreen(ClaimBeaconMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		CAWClientGUIManager.setClaimInfoSize(3, 3);
		this.imageHeight = 229;
		this.imageWidth = 240;
		this.inventoryLabelY = 134;
	}
	
	@Override
	protected void init() {
		super.init();
		x = (width - this.imageWidth) / 2;
		y = (height - this.imageHeight) / 2;
		this.mapWidget = this.addRenderableWidget(new MapWidgetInGui(x + 8, y + 17));
		
		this.button1 = Button.builder(Component.literal("------"), (button) -> {
			CAWConstants.debugLog("button 1 pressed");
			this.button1Press();
		}).pos(x + 178, y + 99).size(53, 20).build();
		this.addRenderableWidget(button1);
		
		this.button2 = Button.builder(Component.literal("------"), (button) -> {
			CAWConstants.debugLog("button 2 pressed");
			this.button2Press();
		}).pos(x + 178, y + 127).size(53, 20).build();
		this.addRenderableWidget(button2);
		
		this.button3 = Button.builder(Component.literal("------"), (button) -> {
			CAWConstants.debugLog("button 3 pressed");
			this.button3Press();
		}).pos(x + 178, y + 155).size(53, 20).build();
		this.addRenderableWidget(button3);
		
		this.remainFuelTime = new StringWidget(x + 147, y + 18, 77, 11, Component.literal("--:--:--"), this.font);
		this.size = new StringWidget(x + 147, y + 30, 77, 11, Component.literal("0 x 0"), this.font);
		this.ownerName = new StringWidget(x + 147, y + 42, 77, 11, Component.literal("------"), this.font);
		this.factionName = new StringWidget(x + 147, y + 54, 77, 11, Component.literal("------"), this.font);
		this.upgradeCount = new StringWidget(x + 147, y + 66, 77, 11, Component.literal("------"), this.font);
		this.addRenderableWidget(remainFuelTime);
		this.addRenderableWidget(size);
		this.addRenderableWidget(ownerName);
		this.addRenderableWidget(factionName);
		this.addRenderableWidget(upgradeCount);
		
		this.initialized = true;
	}
	
	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		graphics.blit(UI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	protected void renderClaimAreaSize(GuiGraphics graphics, float delta, int size) {
		int offset = 16 * (4 - size);
		int sideLength = 16 * (2 * size - 1) + 1;
		int startX = x + 8 + offset;
		int startY = y + 17 + offset;
		graphics.renderOutline(startX, startY, sideLength, sideLength, ColorUtil.combine(255, 200, 200, 0));
		graphics.renderOutline(startX + 1, startY + 1, sideLength - 2, sideLength - 2, ColorUtil.combine(255, 200, 200, 0));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float delta) {
		super.renderBackground(graphics);
		
		super.render(graphics, pMouseX, pMouseY, delta);
		
		renderClaimAreaSize(graphics, delta, this.claimSize);
		
		drawStatusLight(graphics);
		drawIcons(graphics, pMouseX, pMouseY);
		
		setProgress(graphics, 0, this.progresses[0]);
		setProgress(graphics, 1, this.progresses[1]);
		setProgress(graphics, 2, this.progresses[2]);
		drawRemainingFuel(graphics, pMouseX, pMouseY);
		
		renderTooltip(graphics, pMouseX, pMouseY);
	}
	
	@SuppressWarnings("DuplicatedCode")
	@Override
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		if (this.mapWidget == null) {
			super.renderTooltip(graphics, mouseX, mouseY);
			return;
		}
		if (mapWidget.isMouseInBounds(mouseX, mouseY)) {
			Vector2i relativePos = mapWidget.getRelativeMousePos(mouseX, mouseY);
			Vector2i worldPos = new Vector2i(relativePos.x + CAWClientGUIManager.mapPos.x, relativePos.y + CAWClientGUIManager.mapPos.y);
			Vector2i chunkPos = new Vector2i(relativePos.x >> 4, relativePos.y >> 4);
			Vector2i chunkInChunk = new Vector2i(relativePos.x & 15, relativePos.y & 15);
			graphics.renderTooltip(this.font,
			this.addTooltip(relativePos, worldPos, chunkPos, chunkInChunk), Optional.empty(),
			mouseX, mouseY
			);
		} else {
			super.renderTooltip(graphics, mouseX, mouseY);
		}
	}
	
	private boolean isInToBeClaimedRange(Vector2i relativePos, int size) {
		int offset = 16 * (4 - size);
		int sideLength = 16 * (2 * size - 1) + 1;
		return relativePos.x >= offset && relativePos.y >= offset && relativePos.x < offset + sideLength && relativePos.y < offset + sideLength;
	}
	
	@SuppressWarnings("DuplicatedCode")
	public List<Component> addTooltip(Vector2i relativePos, Vector2i posXZ, Vector2i posXZChunk, Vector2i posXZInChunk) {
		List<Component> result = new ArrayList<>();
		result.add(Component.literal("X: " + (posXZ.x) + " Z: " + (posXZ.y)));
		if (CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y] != null) {
			result.add(CAWClientGUIManager.claimedChunkInfo[posXZChunk.x][posXZChunk.y].Name);
		}
		if (isInToBeClaimedRange(relativePos, claimSize)) {
			result.add(Component.translatable("caw.gui.common.will_be_claimed"));
		}
		return result;
	}
	
	@Override
	protected void containerTick() {
		super.containerTick();
		if (!this.initialized) return;
		
		if (this.mapWidget != null) this.mapWidget.tick();
		
		int fuelSeconds = this.menu.block.getRemainingFuel() / this.menu.block.getFuelConsumption();
		int fuelMinutes = fuelSeconds / 60;
		int fuelHours = fuelMinutes / 60;
		
		this.remainFuelTime.setMessage(Component.literal(String.format("%02d:%02d:%02d",
		fuelHours,
		(fuelMinutes % 60),
		(fuelSeconds % 60))
		));
		
		this.ownerName.setMessage(Component.literal(this.menu.getOwnerName()));
		this.factionName.setMessage(Component.literal(this.menu.getFactionName()));
		this.claimSize = this.menu.getClaimSize();
		int sizeDisp = (this.claimSize * 2) - 1;
		this.size.setMessage(Component.literal(sizeDisp + " x " + sizeDisp));
		this.upgradeCount.setMessage(Component.literal(String.valueOf(this.menu.block.getUpgradeAmount())));
		
		this.updateProgressBar1();
		this.updateProgressBar2();
		
		this.updateButton1();
		this.updateButton2();
		this.updateButton3();
	}
	
	protected void button1Press() {
		int status = this.menu.block.getStatus();
		if (BeaconHelper.isProperOff(status)) {
			ChannelRegistry.sendToServer(new PTSBeaconInstructionPacket(BeaconPacketHandler.INST_ON));
		} else if (status == ClaimBeaconBlockEntity.STATUS_RUNNING) {
			ChannelRegistry.sendToServer(new PTSBeaconInstructionPacket(BeaconPacketHandler.INST_OFF));
		}
	}
	
	protected void button2Press() {
		int status = this.menu.block.getStatus();
		if (BeaconHelper.isOff(status)) {
			ChannelRegistry.sendToServer(new PTSBeaconInstructionPacket(BeaconPacketHandler.INST_CHANGE_FACTION));
		}
	}
	
	protected void button3Press() {
		int status = this.menu.block.getStatus();
		if (status == ClaimBeaconBlockEntity.STATUS_STARTING ||
		    status == ClaimBeaconBlockEntity.STATUS_STOPPING ||
		    status == ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION ||
		    status == ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION
		) {
			ChannelRegistry.sendToServer(new PTSBeaconInstructionPacket(BeaconPacketHandler.INST_CANCEL));
		}
	}
	
	protected void updateButton1() {
		int status = this.menu.block.getStatus();
		if (!this.menu.block.isFactionValidClientSide()) {
			this.button1.active = false;
			this.button1.setTooltip(Tooltip.create(Component.translatable("caw.gui.error.set_faction")));
			return;
		}
		this.button1.setTooltip(null);
		if (BeaconHelper.isProperOff(status)) {
			this.button1.active = true;
			this.button1.setMessage(Component.translatable("caw.gui.button.start"));
		} else if (status == ClaimBeaconBlockEntity.STATUS_RUNNING) {
			this.button1.active = true;
			this.button1.setMessage(Component.translatable("caw.gui.button.stop"));
		} else if (status == ClaimBeaconBlockEntity.STATUS_STARTING) {
			this.button1.active = false;
			this.button1.setMessage(Component.translatable("caw.gui.button.starting"));
		} else if (status == ClaimBeaconBlockEntity.STATUS_STOPPING || status == ClaimBeaconBlockEntity.STATUS_ERRORED_STOPPING) {
			this.button1.active = false;
			this.button1.setMessage(Component.translatable("caw.gui.button.stopping"));
		} else {
			this.button1.active = false;
			this.button1.setMessage(Component.translatable("caw.gui.label.empty"));
		}
	}
	
	protected void updateButton2() {
		int status = this.menu.block.getStatus();
		if (BeaconHelper.isOff(status)) {
			this.button2.active = true;
			this.button2.setMessage(Component.translatable("caw.gui.button.set_faction"));
			this.button2.setTooltip(null);
		} else if (status == ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION || status == ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION) {
			this.button2.active = true;
			this.button2.setMessage(Component.translatable("caw.gui.button.configuring"));
			this.button2.setTooltip(null);
		} else {
			this.button2.active = false;
			this.button2.setTooltip(Tooltip.create(Component.translatable("caw.gui.error.only_configure_when_off")));
			this.button2.setMessage(Component.translatable("caw.gui.label.empty"));
		}
	}
	
	protected void updateButton3() {
		int status = this.menu.block.getStatus();
		if (status == ClaimBeaconBlockEntity.STATUS_STARTING ||
		    status == ClaimBeaconBlockEntity.STATUS_STOPPING ||
		    status == ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION ||
		    status == ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION
		) {
			this.button3.setMessage(Component.translatable("caw.gui.common.cancel"));
			if (CAWClientDataManager.isClientPlayerInFaction(this.menu.block.getOwningFactionUUID())) {
				this.button3.active = true;
				this.button3.setTooltip(null);
			} else {
				this.button3.active = false;
				this.button3.setTooltip(Tooltip.create(Component.translatable("caw.gui.error.must_be_in_same_faction")));
			}
		} else {
			this.button3.setMessage(Component.translatable("caw.gui.label.empty"));
			this.button3.active = false;
			this.button3.setTooltip(null);
		}
	}
	
	protected void drawStatusLight(GuiGraphics graphics) {
		int status = this.menu.block.getStatus();
		boolean flash = this.menu.block.getTimer() % 2 == 0;
		switch (status) {
			case ClaimBeaconBlockEntity.STATUS_OFF:
				drawIndicatorLight(graphics, 180, 89, 0);
				break;
			case ClaimBeaconBlockEntity.STATUS_RUNNING:
				drawIndicatorLight(graphics, 180, 89, 2);
				drawIndicatorLight(graphics, 191, 89, 2);
				break;
			case ClaimBeaconBlockEntity.STATUS_STARTING:
				drawIndicatorLight(graphics, 180, 89, 1);
				if (flash) drawIndicatorLight(graphics, 191, 89, 2);
				break;
			case ClaimBeaconBlockEntity.STATUS_STOPPING:
				drawIndicatorLight(graphics, 180, 89, 1);
				if (flash) drawIndicatorLight(graphics, 191, 89, 4);
				break;
			case ClaimBeaconBlockEntity.STATUS_ERRORED_STOPPING:
				drawIndicatorLight(graphics, 180, 89, 4);
				if (flash) drawIndicatorLight(graphics, 191, 89, 4);
				if (!flash) drawIndicatorLight(graphics, 202, 89, 0);
				break;
			case ClaimBeaconBlockEntity.STATUS_ERRORED_OFF:
				drawIndicatorLight(graphics, 180, 89, 0);
				drawIndicatorLight(graphics, 202, 89, 0);
				break;
			case ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION:
				drawIndicatorLight(graphics, 180, 89, 1);
				if (flash) drawIndicatorLight(graphics, 191, 89, 3);
				break;
		}
		
	}
	
	protected void updateProgressBar1() {
		switch (this.menu.block.getStatus()) {
			case ClaimBeaconBlockEntity.STATUS_STARTING:
			case ClaimBeaconBlockEntity.STATUS_STOPPING:
			case ClaimBeaconBlockEntity.STATUS_ERRORED_STOPPING:
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_STOPPING:
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION:
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_REPAIRING:
				this.progresses[0] = 1f - (float) this.menu.block.getTimer() / this.menu.block.getTransientTime(this.menu.block.getStatus());
				break;
			default:
				this.progresses[0] = 0f;
		}
	}
	
	protected void updateProgressBar2() {
		switch (this.menu.block.getStatus()) {
			case ClaimBeaconBlockEntity.STATUS_CHANGING_FACTION:
				this.progresses[1] = 1f - (float) this.menu.block.getTimer() / this.menu.block.getTransientTime(this.menu.block.getStatus());
				break;
			case ClaimBeaconBlockEntity.STATUS_UNSTABLE_CHANGING_FACTION:
				this.progresses[1] = 1f - (float) this.menu.block.getTimer2() / this.menu.block.getTransientTime(this.menu.block.getStatus());
				break;
			default:
				this.progresses[1] = 0f;
		}
	}
	
	
	private void drawRemainingFuel(GuiGraphics graphics, int mouseX, int mouseY) {
		int fuel = this.menu.block.getRemainingFuel();
		int maxFuel = this.menu.block.getMaxFuel();
		int barHeight = (int) (fuel / (float) maxFuel * 16);
		int yStart = 16 - barHeight;
		graphics.blit(UI_TEXTURE, x + 152, y + 106 + yStart, 44, 230 + yStart, 2, barHeight);
	}
	
	private void drawIcons(GuiGraphics graphics, int mouseX, int mouseY) {
		if (mapWidget == null) return;
		BlockPos selfPos = this.menu.block.getBlockPos();
		Vector2i selfIconPos = mapWidget.worldCoordToRelativeCoord(selfPos.getX(), selfPos.getZ());
		graphics.blit(UI_TEXTURE, selfIconPos.x - 3, selfIconPos.y - 7, 48, 230, 6, 8);
	}
	
	protected void setProgress(GuiGraphics graphics, int id, float percent) {
		int tY = 120 + (id) * 28;
		int tX = 179;
		int actualBarWidth = Math.round(51 * percent);
		graphics.blit(UI_TEXTURE, x + tX, y + tY, 0, 249, actualBarWidth, 2);
	}
	
	int[][] indicatorPos = new int[][]{
	new int[]{21, 230},
	new int[]{28, 230},
	new int[]{35, 230},
	new int[]{21, 238},
	new int[]{28, 238},
	new int[]{35, 238},
	};
	
	protected void drawIndicatorLight(GuiGraphics graphics, int px, int py, int type) {
		graphics.blit(UI_TEXTURE, x + px, y + py, indicatorPos[type][0], indicatorPos[type][1], 7, 7);
	}
}
