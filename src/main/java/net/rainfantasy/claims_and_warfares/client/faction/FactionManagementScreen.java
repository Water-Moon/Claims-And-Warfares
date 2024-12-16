package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.*;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FactionManagementScreen extends Screen {
	
	public FactionManagementScreen() {
		super(Component.literal("Faction Management"));
	}
	
	public static final int BUTTON_WIDTH = 150;
	public static final int BUTTON_HEIGHT = 20;
	
	StringWidget currentFactionName;
	Button selectFactionButton;
	
	Button viewPendingInviteButton;
	Button createPageButton;
	Button invitePageButton;
	Button transferButton;
	Button memberPageButton;
	Button diplomacyPageButton;
	Button settingsPageButton;
	Button disbandPageButton;
	Button leavePageButton;
	
	Button cancelButton;
	
	private static Component getFactionDisplay() {
		return Component.translatable(
		"caw.gui.label.current_selected",
		CAWClientDataManager.getCurrentSelectedFactionName(),
		CAWClientDataManager.getPermissionInCurrentSelectedFaction()
		);
	}
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int y_start = yCenter - 50;
		
		currentFactionName = new StringWidget(0, 0, 200, 20, getFactionDisplay(), this.font);
		currentFactionName.alignLeft();
		selectFactionButton = Button.builder(Component.translatable("caw.gui.button.select_primary"), btn -> {
			openSelectScreen();
		}).pos(10, 20).size(100, 20).build();
		this.addRenderableWidget(currentFactionName);
		this.addRenderableWidget(selectFactionButton);
		
		viewPendingInviteButton = Button.builder(Component.translatable("caw.gui.button.invitations_with_count", 0), btn -> {
			openViewInvitationScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start - BUTTON_HEIGHT).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		viewPendingInviteButton.active = false;
		this.addRenderableWidget(viewPendingInviteButton);
		
		createPageButton = Button.builder(Component.translatable("caw.gui.button.create_faction"), btn -> {
			openCreateScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		this.addRenderableWidget(createPageButton);
		
		invitePageButton = Button.builder(Component.translatable("caw.gui.button.invite_players"), btn -> {
			openInvitationScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		this.addRenderableWidget(invitePageButton);
		
		diplomacyPageButton = Button.builder(Component.translatable("caw.gui.button.diplomacy"), btn -> {
			openDiplomacyScreen();
			CAWConstants.debugLog("Diplomacy button pressed");
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT * 2).size(BUTTON_WIDTH / 2, BUTTON_HEIGHT).build();
		this.addRenderableWidget(diplomacyPageButton);
		
		settingsPageButton = Button.builder(Component.translatable("caw.gui.button.faction_settings"), btn -> {
			openSettingsScreen();
			CAWConstants.debugLog("Settings button pressed");
		}).pos(xCenter, y_start + BUTTON_HEIGHT * 2).size(BUTTON_WIDTH / 2, BUTTON_HEIGHT).build();
		this.addRenderableWidget(settingsPageButton);
		
		
		memberPageButton = Button.builder(Component.translatable("caw.gui.button.manage_members"), btn -> {
			openManageScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT * 3).size(BUTTON_WIDTH / 2, BUTTON_HEIGHT).build();
		this.addRenderableWidget(memberPageButton);
		
		transferButton = Button.builder(Component.translatable("caw.gui.button.transfer_faction"), btn -> {
			openTransferScreen();
		}).pos(xCenter, y_start + BUTTON_HEIGHT * 3).size(BUTTON_WIDTH / 2, BUTTON_HEIGHT).build();
		this.addRenderableWidget(transferButton);
		
		disbandPageButton = Button.builder(Component.translatable("caw.gui.button.disband_faction"), btn -> {
			openDisbandScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT * 4).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		this.addRenderableWidget(disbandPageButton);
		
		leavePageButton = Button.builder(Component.translatable("caw.gui.button.leave_faction"), btn -> {
			openLeaveFactionScreen();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT * 5).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		this.addRenderableWidget(leavePageButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.back"), btn -> {
			this.onClose();
		}).pos(xCenter - BUTTON_WIDTH / 2, y_start + BUTTON_HEIGHT * 6 + 5).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
		this.addRenderableWidget(cancelButton);
	}
	
	private void refresh() {
		currentFactionName.setMessage(getFactionDisplay());
		CAWClientDataManager.getClientPlayerUUID().flatMap(CAWClientDataManager::findInvitationTo).ifPresentOrElse(invitations -> {
			viewPendingInviteButton.setMessage(Component.translatable("caw.gui.button.invitations_with_count", invitations.size()));
			viewPendingInviteButton.active = (!invitations.isEmpty());
		}, () -> {
			viewPendingInviteButton.setMessage(Component.translatable("caw.gui.button.invitations_with_count", 0));
			viewPendingInviteButton.active = false;
		});
		
		boolean clientPlayerExists = CAWClientDataManager.getClientPlayerUUID().isPresent();
		boolean clientPlayerHasFaction = CAWClientDataManager.getCurrentSelectedFaction().isPresent();
		boolean clientPlayerIsFactionOwner = false;
		boolean clientPlayerIsFactionAdmin = false;
		
		if (clientPlayerHasFaction && clientPlayerExists) {
			UUID playerUUID = CAWClientDataManager.getClientPlayerUUID().get();
			clientPlayerIsFactionOwner = CAWClientDataManager.getCurrentSelectedFaction()
			                             .map(faction -> faction.isPlayerOwner(playerUUID))
			                             .orElse(false);
			clientPlayerIsFactionAdmin = CAWClientDataManager.getCurrentSelectedFaction()
			                             .map(faction -> faction.isPlayerAdmin(playerUUID))
			                             .orElse(false) || clientPlayerIsFactionOwner;
		}
		
		selectFactionButton.active = clientPlayerExists;
		createPageButton.active = clientPlayerExists;
		invitePageButton.active = clientPlayerHasFaction && clientPlayerExists;
		transferButton.active = clientPlayerHasFaction && clientPlayerIsFactionOwner;
		memberPageButton.active = clientPlayerHasFaction;
		diplomacyPageButton.active = clientPlayerIsFactionAdmin;
		disbandPageButton.active = clientPlayerHasFaction && clientPlayerIsFactionOwner;
		leavePageButton.active = clientPlayerHasFaction;
	}
	
	@Override
	public void tick() {
		super.tick();
		refresh();
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}
	
	
	static void onSubPageCancel() {
		ChannelRegistry.sendToServer(new PTSOpenFactionManagePacket());
	}
	
	void openSelectScreen() {
		ChannelRegistry.sendToServer(new PTSOpenSelectActiveFactionPacket());
	}
	
	void openViewInvitationScreen() {
		ChannelRegistry.sendToServer(new PTSOpenViewInvitationPacket());
	}
	
	void openCreateScreen() {
		ChannelRegistry.sendToServer(new PTSOpenFactionCreatePacket());
	}
	
	void openInvitationScreen() {
		ChannelRegistry.sendToServer(new PTSOpenFactionInvitePacket());
	}
	
	void openManageScreen() {
		ChannelRegistry.sendToServer(new PTSOpenMemberManagePacket());
	}
	
	void openTransferScreen() {
		ChannelRegistry.sendToServer(new PTSOpenTransferFactionPacket());
	}
	
	void openDisbandScreen() {
		ChannelRegistry.sendToServer(new PTSOpenDisbandFactionPacket());
	}
	
	void openLeaveFactionScreen() {
		ChannelRegistry.sendToServer(new PTSOpenLeaveFactionPacket());
	}
	
	void openDiplomacyScreen() {
		ChannelRegistry.sendToServer(new PTSOpenRelationshipPacket());
	}
	
	void openSettingsScreen() {
		ChannelRegistry.sendToServer(new PTSOpenFactionSettingPacket());
	}
}
