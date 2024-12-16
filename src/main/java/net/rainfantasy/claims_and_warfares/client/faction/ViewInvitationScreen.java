package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSAcceptInvitationPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSRejectInvitationPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ViewInvitationScreen extends Screen {
	
	private static final int LINE_HEIGHT = 20;
	private static final int LINE_WIDTH = 200;
	private static final int LABEL_WIDTH = 100;
	private static final int BUTTON_WIDTH = 100;
	private static final int TOTAL_LINE_WIDTH = LINE_WIDTH + LABEL_WIDTH;
	private static final int TOTAL_LINES = 5;
	
	StringWidget factionName;
	StringWidget factionNameLabel;
	StringWidget fromPlayerName;
	StringWidget fromPlayerNameLabel;
	StringWidget sentTime;
	StringWidget sentTimeLabel;
	StringWidget pageNumber;
	StringWidget message;
	
	Button previousPageButton;
	Button nextPageButton;
	Button acceptButton;
	Button declineButton;
	Button cancelButton;
	
	List<InvitationInfo> invitations = new ArrayList<>();
	InvitationInfo currentInvitation = null;
	int currentPage = 1;
	
	
	
	public ViewInvitationScreen() {
		super(Component.literal("View Invitation"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int yStart = yCenter - (LINE_HEIGHT * TOTAL_LINES) / 2;
		int lineXStart = xCenter - TOTAL_LINE_WIDTH / 2;
		
		factionNameLabel = new StringWidget(lineXStart, yStart, LABEL_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.faction_name"), this.font);
		addRenderableWidget(factionNameLabel);
		factionName = new StringWidget(lineXStart + LABEL_WIDTH, yStart, LINE_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
		addRenderableWidget(factionName);
		
		fromPlayerNameLabel = new StringWidget(lineXStart, yStart + LINE_HEIGHT, LABEL_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.invitation_from"), this.font);
		addRenderableWidget(fromPlayerNameLabel);
		fromPlayerName = new StringWidget(lineXStart + LABEL_WIDTH, yStart + LINE_HEIGHT, LINE_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
		addRenderableWidget(fromPlayerName);
		
		sentTimeLabel = new StringWidget(lineXStart, yStart + 2 * LINE_HEIGHT, LABEL_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.invitation_time"), this.font);
		addRenderableWidget(sentTimeLabel);
		sentTime = new StringWidget(lineXStart + LABEL_WIDTH, yStart + 2 * LINE_HEIGHT, LINE_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
		addRenderableWidget(sentTime);
		
		message = new StringWidget(xCenter - 100, yStart + 3 * LINE_HEIGHT, 200, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
		addRenderableWidget(message);
		
		acceptButton = Button.builder(Component.translatable("caw.gui.common.accept"), btn -> {
			this.onAcceptButtonPressed();
		}).pos(xCenter - BUTTON_WIDTH - 50, yStart + 4 * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(acceptButton);
		
		declineButton = Button.builder(Component.translatable("caw.gui.common.decline"), btn -> {
			this.onDeclineButtonPressed();
		}).pos(xCenter + 50, yStart + 4 * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(declineButton);
		
		previousPageButton = Button.builder(Component.translatable("caw.gui.common.prev_page"), btn -> {
			this.changePage(-1);
		}).pos(xCenter - BUTTON_WIDTH - 50, yStart + 5 * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		nextPageButton = Button.builder(Component.translatable("caw.gui.common.next_page"), btn -> {
			this.changePage(1);
		}).pos(xCenter + 50, yStart + 5 * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(previousPageButton);
		addRenderableWidget(nextPageButton);
		
		pageNumber = new StringWidget(xCenter - 50, yStart + 5 * LINE_HEIGHT, 100, LINE_HEIGHT, Component.translatable("caw.gui.common.a_of_b", 0, 0), this.font);
		addRenderableWidget(pageNumber);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.cancel"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xCenter - BUTTON_WIDTH / 2, yStart + 6 * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(cancelButton);
	}
	
	public void onAcceptButtonPressed(){
		CAWConstants.debugLog("Accept button pressed");
		if(currentInvitation == null) return;
		ChannelRegistry.sendToServer(new PTSAcceptInvitationPacket(currentInvitation.getInvitationUUID()));
	}
	
	public void onDeclineButtonPressed(){
		CAWConstants.debugLog("Decline button pressed");
		if(currentInvitation == null) return;
		ChannelRegistry.sendToServer(new PTSRejectInvitationPacket(currentInvitation.getInvitationUUID()));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}
	
	private void copyInvitationInfo(){
		invitations.clear();
		CAWClientDataManager.findInvitationToSelf().ifPresent(list -> {
			invitations.addAll(list);
		});
	}
	
	private void changePage(int delta){
		currentPage += delta;
		ensurePageNumberValid();
		populatePage();
	}
	
	private void ensurePageNumberValid(){
		if(currentPage < 1){
			currentPage = 1;
		}
		int maxPage = Math.max(1, invitations.size());
		if(currentPage > maxPage){
			currentPage = maxPage;
		}
	}
	
	private void setPageNumber(){
		pageNumber.setMessage(Component.translatable("caw.gui.common.a_of_b", currentPage, invitations.size()));
	}
	
	private void populatePageInvalid(){
		factionName.setMessage(Component.translatable("caw.gui.label.empty"));
		fromPlayerName.setMessage(Component.translatable("caw.gui.label.empty"));
		sentTime.setMessage(Component.translatable("caw.gui.label.empty"));
		
		acceptButton.active = false;
		declineButton.active = false;
	}
	
	private void populatePageValid(){
		factionName.setMessage(Component.literal(currentInvitation.getFactionName()));
		fromPlayerName.setMessage(Component.literal(currentInvitation.getFromPlayerName()));
		sentTime.setMessage(currentInvitation.getSentTime().format());
		
		acceptButton.active = true;
		declineButton.active = true;
	}
	
	private void populatePage(){
		if(this.currentPage < 1 || this.currentPage > invitations.size()){
			populatePageInvalid();
		}else{
			currentInvitation = invitations.get(currentPage - 1);
			populatePageValid();
		}
		setPageNumber();
	}
	
	private void updateMessage(){
		this.message.setMessage(CAWClientGUIManager.getLastMessage().orElse(Component.empty()));
	}
	
	private void refresh(){
		copyInvitationInfo();
		ensurePageNumberValid();
		populatePage();
		updateMessage();
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
}
