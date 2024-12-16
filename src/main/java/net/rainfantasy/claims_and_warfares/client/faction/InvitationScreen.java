package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientPlayerData;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSCancelInvitePacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSInvitePlayerPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InvitationScreen extends Screen {
	private static final int PLAYERS_PER_PAGE = 5;
	
	private static final int LINE_HEIGHT = 20;
	private static final int NAME_WIDTH = 150;
	private static final int BUTTON_WIDTH = 50;
	private static final int TOTAL_LINES = PLAYERS_PER_PAGE + 3;
	
	private static final int INVITE_LINES_WIDTH = NAME_WIDTH + BUTTON_WIDTH;
	
	private static final int PAGE_BUTTON_WIDTH = 100;
	
	
	StringWidget[] playerNames = new StringWidget[PLAYERS_PER_PAGE];
	StringWidget message;
	Button[] inviteButtons = new Button[PLAYERS_PER_PAGE];
	Button nextPageButton;
	Button previousPageButton;
	Button cancelButton;
	
	int currentPage = 1;
	List<UUID> players = new ArrayList<>();
	UUID[] currentPagePlayers = new UUID[PLAYERS_PER_PAGE];
	
	
	public InvitationScreen() {
		super(Component.literal("Invite Players"));
	}
	
	@SuppressWarnings("DuplicatedCode")
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int inviteLineXStart = xCenter - INVITE_LINES_WIDTH / 2;
		int yStart = yCenter - (LINE_HEIGHT * TOTAL_LINES) / 2;
		
		for (int i = 0; i < PLAYERS_PER_PAGE; i++) {
			playerNames[i] = new StringWidget(inviteLineXStart, yStart + LINE_HEIGHT * i, NAME_WIDTH, LINE_HEIGHT, Component.empty(), this.font);
			addRenderableWidget(playerNames[i]);
			
			int finalI = i;
			inviteButtons[i] = Button.builder(Component.empty(), btn -> {
				CAWConstants.debugLog("Invite button {} pressed", finalI);
				this.onInviteButtonPressed(finalI);
			}).pos(inviteLineXStart + NAME_WIDTH, yStart + LINE_HEIGHT * i).size(BUTTON_WIDTH, LINE_HEIGHT).build();
			addRenderableWidget(inviteButtons[i]);
		}
		
		message = new StringWidget(xCenter - 100, yStart + LINE_HEIGHT * PLAYERS_PER_PAGE, 200, LINE_HEIGHT, Component.empty(), this.font);
		addRenderableWidget(message);
		
		previousPageButton = Button.builder(Component.translatable("caw.gui.common.prev_page"), btn -> {
			CAWConstants.debugLog("Previous page button pressed");
			this.changePage(-1);
		}).pos(xCenter - PAGE_BUTTON_WIDTH - 50, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 1)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		nextPageButton = Button.builder(Component.translatable("caw.gui.common.next_page"), btn -> {
			CAWConstants.debugLog("Next page button pressed");
			this.changePage(1);
		}).pos(xCenter + 50, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 1)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(previousPageButton);
		addRenderableWidget(nextPageButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.back"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xCenter - PAGE_BUTTON_WIDTH / 2, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 2)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(cancelButton);
		
	}
	
	private void onInviteButtonPressed(int index){
		UUID playerUUID = currentPagePlayers[index];
		UUID currentFaction = CAWClientDataManager.getCurrentSelectedFaction().map(ClientFactionData::getFactionUUID).orElse(null);
		boolean hasInvitation = anyInvitationTo(playerUUID);
		if(hasInvitation){
			CAWConstants.debugLog("Withdrawing invitation to player {}", playerUUID);
			ChannelRegistry.sendToServer(new PTSCancelInvitePacket(playerUUID, currentFaction));
		}else{
			CAWConstants.debugLog("Inviting player {} to faction {}", playerUUID, currentFaction);
			ChannelRegistry.sendToServer(new PTSInvitePlayerPacket(playerUUID, currentFaction));
		}
	}
	
	private static boolean anyInvitationTo(UUID playerUUID){
		Optional<UUID> selfUUID = CAWClientDataManager.getClientPlayerUUID();
		UUID currentFactionUUID = CAWClientDataManager.getCurrentSelectedFaction().map(ClientFactionData::getFactionUUID).orElse(null);
		if(selfUUID.isEmpty() || currentFactionUUID == null){
			return false;
		}
		return CAWClientDataManager.findInvitationToPlayerForFaction(playerUUID, currentFactionUUID).isPresent();
	}
	
	private void setButtonMessage(int index, UUID playerUUID){
		if(anyInvitationTo(playerUUID)){
			inviteButtons[index].setMessage(Component.translatable("caw.gui.button.cancel_invite"));
		}else{
			inviteButtons[index].setMessage(Component.translatable("caw.gui.button.invite"));
		}
	}
	
	private void setupPlayerButton(int indexInList, int index){
		UUID playerUUID = players.get(indexInList);
		Optional<ClientPlayerData> data = CAWClientDataManager.getPlayerData(playerUUID);
		data.ifPresentOrElse(playerData -> {
			playerNames[index].setMessage(Component.literal(playerData.getPlayerName()));
			currentPagePlayers[index] = playerUUID;
			if(CAWClientDataManager.isPlayerInCurrentSelectedFaction(playerUUID)){
				inviteButtons[index].active = false;
				inviteButtons[index].setMessage(Component.translatable("caw.gui.button.is_member"));
			} else {
				inviteButtons[index].active = true;
				setButtonMessage(index, playerUUID);
			}
		}, () -> {
			CAWClientDataManager.getFallbackPlayerNameFromInvitation(playerUUID).ifPresentOrElse(
				name -> {
					currentPagePlayers[index] = playerUUID;
					playerNames[index].setMessage(Component.literal(name).append(
						Component.translatable("caw.gui.format.bracket",
							Component.translatable("caw.gui.common.offline")
						)
					));
					inviteButtons[index].active = true;
					setButtonMessage(index, playerUUID);
				},
				() -> setupButtonNoPlayer(index)
			);
		});
	}
	
	private void setupButtonNoPlayer(int index){
		currentPagePlayers[index] = null;
		playerNames[index].setMessage(Component.translatable("caw.gui.label.empty"));
		inviteButtons[index].active = false;
	}
	
	private void changePage(int delta){
		currentPage += delta;
		ensurePageNumberValid();
		populateCurrentPage();
	}
	
	private void ensurePageNumberValid(){
		if(currentPage < 1){
			currentPage = 1;
		}
		int maxPage = Math.max(1, (int) Math.ceil((double) players.size() / PLAYERS_PER_PAGE));
		if(currentPage > maxPage){
			currentPage = maxPage;
		}
	}
	
	private void populateCurrentPage(){
		for(int i = 0; i < PLAYERS_PER_PAGE; i++){
			int playerIndex = (currentPage - 1) * PLAYERS_PER_PAGE + i;
			if(playerIndex < players.size()){
				setupPlayerButton(playerIndex, i);
			}else{
				setupButtonNoPlayer(i);
			}
		}
	}
	
	private void copyPlayerData(){
		players.clear();
		Set<UUID> currentPlayerAndInvitedPlayers = new HashSet<>();
		currentPlayerAndInvitedPlayers.addAll(CAWClientDataManager.getPlayerUUIDsExcludeSelf());
		currentPlayerAndInvitedPlayers.addAll(CAWClientDataManager.getUUIDsOfPlayersWhoHasInvitationFromClientPlayer());
		players.addAll(currentPlayerAndInvitedPlayers);
	}
	
	private void updateMessage(){
		CAWClientGUIManager.getLastMessage().ifPresentOrElse(
			message::setMessage,
			() -> message.setMessage(Component.empty())
		);
	}
	
	private void refresh(){
		copyPlayerData();
		ensurePageNumberValid();
		populateCurrentPage();
		updateMessage();
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
	
	@Override
	public void tick() {
		super.tick();
		this.refresh();
	}
}
