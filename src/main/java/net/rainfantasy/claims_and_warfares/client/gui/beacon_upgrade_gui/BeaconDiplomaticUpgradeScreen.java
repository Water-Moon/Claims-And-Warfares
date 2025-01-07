package net.rainfantasy.claims_and_warfares.client.gui.beacon_upgrade_gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_menus.BeaconDiplomaticUpgradeMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon_upgrade.PTSDiplomaticUpgradeSetPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("FieldCanBeLocal")
public class BeaconDiplomaticUpgradeScreen extends AbstractContainerScreen<BeaconDiplomaticUpgradeMenu> {
	
	public BeaconDiplomaticUpgradeScreen(BeaconDiplomaticUpgradeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}
	
	private StringWidget labelBreakPlace;
	private StringWidget labelInteract;
	
	private Button buttonBreakPlaceOwner;
	private Button buttonBreakPlaceAllies;
	private Button buttonBreakPlaceNeutral;
	private Button buttonInteractOwner;
	private Button buttonInteractAllies;
	private Button buttonInteractNeutral;
	
	public static final int BUTTON_WIDTH = 50;
	public static final int LABEL_WIDTH = 100;
	public static final int LINE_HEIGHT = 20;
	public static final int LINE_COUNT = 2;
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int xStart = xCenter - LABEL_WIDTH - BUTTON_WIDTH;
		int yStart = yCenter - (LINE_HEIGHT * LINE_COUNT) / 2;
		
		labelBreakPlace = new StringWidget(xStart, yStart, LABEL_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.perm_break_place"), this.font);
		addRenderableWidget(labelBreakPlace);
		labelInteract = new StringWidget(xStart, yStart + LINE_HEIGHT, LABEL_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.perm_interact"), this.font);
		addRenderableWidget(labelInteract);
		
		buttonBreakPlaceOwner = Button.builder(Component.translatable("caw.string.faction.owner"), btn -> {
			sendSetting(FactionOwnedClaimFeature.BREAK_PLACE, DiplomaticRelationshipData.OWNER);
		}).pos(xStart + LABEL_WIDTH, yStart).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonBreakPlaceOwner);
		buttonBreakPlaceAllies = Button.builder(Component.translatable("caw.string.faction.ally"), btn -> {
			sendSetting(FactionOwnedClaimFeature.BREAK_PLACE, DiplomaticRelationshipData.ALLY);
		}).pos(xStart + LABEL_WIDTH + BUTTON_WIDTH, yStart).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonBreakPlaceAllies);
		buttonBreakPlaceNeutral = Button.builder(Component.translatable("caw.string.faction.neutral"), btn -> {
			sendSetting(FactionOwnedClaimFeature.BREAK_PLACE, DiplomaticRelationshipData.NEUTRAL);
		}).pos(xStart + LABEL_WIDTH + BUTTON_WIDTH * 2, yStart).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonBreakPlaceNeutral);
		
		buttonInteractOwner = Button.builder(Component.translatable("caw.string.faction.owner"), btn -> {
			sendSetting(FactionOwnedClaimFeature.INTERACT, DiplomaticRelationshipData.OWNER);
		}).pos(xStart + LABEL_WIDTH, yStart + LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonInteractOwner);
		buttonInteractAllies = Button.builder(Component.translatable("caw.string.faction.ally"), btn -> {
			sendSetting(FactionOwnedClaimFeature.INTERACT, DiplomaticRelationshipData.ALLY);
		}).pos(xStart + LABEL_WIDTH + BUTTON_WIDTH, yStart + LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonInteractAllies);
		buttonInteractNeutral = Button.builder(Component.translatable("caw.string.faction.neutral"), btn -> {
			sendSetting(FactionOwnedClaimFeature.INTERACT, DiplomaticRelationshipData.NEUTRAL);
		}).pos(xStart + LABEL_WIDTH + BUTTON_WIDTH * 2, yStart + LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(buttonInteractNeutral);
	}
	
	@Override
	protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
	}
	
	@Override
	public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}
	
	@Override
	protected void containerTick() {
		super.containerTick();
		
		buttonInteractOwner.active = this.menu.block.getInteractPermission() != DiplomaticRelationshipData.OWNER;
		buttonInteractAllies.active = this.menu.block.getInteractPermission() != DiplomaticRelationshipData.ALLY;
		buttonInteractNeutral.active = this.menu.block.getInteractPermission() != DiplomaticRelationshipData.NEUTRAL;
		
		buttonBreakPlaceOwner.active = this.menu.block.getPlaceBreakPermission() != DiplomaticRelationshipData.OWNER;
		buttonBreakPlaceAllies.active = this.menu.block.getPlaceBreakPermission() != DiplomaticRelationshipData.ALLY;
		buttonBreakPlaceNeutral.active = this.menu.block.getPlaceBreakPermission() != DiplomaticRelationshipData.NEUTRAL;
	}
	
	private void sendSetting(int type, int value) {
		ChannelRegistry.sendToServer(new PTSDiplomaticUpgradeSetPacket(type, value));
	}
	
	@Override
	protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {}
}
