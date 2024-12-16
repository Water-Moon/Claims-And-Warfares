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
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSSetPrimaryFactionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SelectCurrentActiveFactionScreen extends Screen {
	private static final int BUTTONS_PER_PAGE = 5;
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int TOTAL_LINES = BUTTONS_PER_PAGE + 3;
	
	StringWidget yourPermissionLabel;
	Button[] selectionButtons = new Button[BUTTONS_PER_PAGE];
	StringWidget[] permissionInfo = new StringWidget[BUTTONS_PER_PAGE];
	Button nextPageButton;
	Button previousPageButton;
	Button cancelButton;
	StringWidget message;
	UUID[] currentPageFactions = new UUID[BUTTONS_PER_PAGE];
	List<UUID> allFactions = new ArrayList<>();
	
	int currentPage = 1;
	
	
	public SelectCurrentActiveFactionScreen() {
		super(Component.literal("Select Current Active Faction"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		
		int yStart = yCenter - (BUTTON_HEIGHT * TOTAL_LINES) / 2;
		int buttonXStart = xCenter - BUTTON_WIDTH / 2;
		
		yourPermissionLabel = new StringWidget(buttonXStart + BUTTON_WIDTH, yStart - BUTTON_HEIGHT, 50, BUTTON_HEIGHT, Component.translatable("caw.gui.label.your_permission"), this.font);
		addRenderableWidget(yourPermissionLabel);
		
		for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
			int finalI = i;
			selectionButtons[i] = Button.builder(Component.empty(), btn -> {
				onButtonPressed(finalI);
			}).pos(buttonXStart, yStart + BUTTON_HEIGHT * i).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
			addRenderableWidget(selectionButtons[i]);
			permissionInfo[i] = new StringWidget(buttonXStart + BUTTON_WIDTH, yStart + BUTTON_HEIGHT * i, 50, BUTTON_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
			addRenderableWidget(permissionInfo[i]);
		}
		
		message = new StringWidget(xCenter - 100, yStart + BUTTON_HEIGHT * BUTTONS_PER_PAGE, 200, BUTTON_HEIGHT, Component.literal("Message"), this.font);
		addRenderableWidget(message);
		
		nextPageButton = Button.builder(Component.translatable("caw.gui.common.next_page"), btn -> {
			CAWConstants.debugLog("Next Page pressed");
			this.changePage(1);
		}).pos(xCenter + 25, yStart + BUTTON_HEIGHT * (BUTTONS_PER_PAGE+1)).size(100, BUTTON_HEIGHT).build();
		addRenderableWidget(nextPageButton);
		
		previousPageButton = Button.builder(Component.translatable("caw.gui.common.prev_page"), btn -> {
			CAWConstants.debugLog("Previous Page pressed");
			this.changePage(-1);
		}).pos(xCenter - 125, yStart + BUTTON_HEIGHT * (BUTTONS_PER_PAGE+1)).size(100, BUTTON_HEIGHT).build();
		addRenderableWidget(previousPageButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.back"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xCenter-50, yStart + BUTTON_HEIGHT * (BUTTONS_PER_PAGE+2)).size(100, BUTTON_HEIGHT).build();
		addRenderableWidget(cancelButton);
	}
	
	/// button handler
	
	private void onButtonPressed(int index){
		CAWConstants.debugLog("Button {} pressed", index);
		if(index < 0 || index >= BUTTONS_PER_PAGE) return;
		UUID factionUUID = currentPageFactions[index];
		if(factionUUID == null) return;
		ChannelRegistry.sendToServer(
			new PTSSetPrimaryFactionPacket(factionUUID)
		);
	}
	
	/// refresh
	
	int refreshCounter = 0;
	private void copyFactionData(){
		if(--refreshCounter > 0) return;
		refreshCounter = 20;
		Optional<UUID> clientPlayerUUID = CAWClientDataManager.getClientPlayerUUID();
		if(clientPlayerUUID.isEmpty()) return;
		allFactions.clear();
		CAWClientDataManager.getFactionUUIDs().forEach(e -> {
			Optional<ClientFactionData> factionData = CAWClientDataManager.getFactionData(e);
			if(factionData.isEmpty()) return;
			if(factionData.get().isPlayerInFaction(clientPlayerUUID.get())){
				allFactions.add(e);
			}
		});
	}
	
	
	private void setupEmptyEntry(int index){
		selectionButtons[index].active = false;
		currentPageFactions[index] = null;
		
		selectionButtons[index].setMessage(Component.empty());
		permissionInfo[index].setMessage(Component.empty());
	}
	
	private void setupValidEntry(int index, int indexInList){
		UUID factionUUID = allFactions.get(indexInList);
		boolean isCurrentSelected = CAWClientDataManager.getCurrentSelectedFaction().map(e -> e.getFactionUUID().equals(factionUUID)).orElse(false);
		Optional<ClientFactionData> factionData = CAWClientDataManager.getFactionData(factionUUID);
		if(factionData.isEmpty()) return;
		
		currentPageFactions[index] = factionUUID;
		permissionInfo[index].setMessage(CAWClientDataManager.getClientPlayerPermissionInFaction(factionUUID));
		if(isCurrentSelected){
			selectionButtons[index].active = false;
			selectionButtons[index].setMessage(
				Component.literal(factionData.get().getFactionName())
				.append(Component.translatable("caw.gui.format.bracket",
					Component.translatable("caw.gui.common.selected")
				))
			);
		}else{
			selectionButtons[index].active = true;
			selectionButtons[index].setMessage(Component.literal(factionData.get().getFactionName()));
		}
	}
	
	private void setupEntry(int index, int indexInList){
		if(indexInList >= allFactions.size()){
			this.setupEmptyEntry(index);
		}else{
			this.setupValidEntry(index, indexInList);
		}
	}
	
	
	private void populatePage(){
		int start = (currentPage - 1) * BUTTONS_PER_PAGE;
		for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
			this.setupEntry(i, start + i);
		}
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
		int maxPage = Math.max(1, (int) Math.ceil((double) allFactions.size() / BUTTONS_PER_PAGE));
		if(currentPage > maxPage){
			currentPage = maxPage;
		}
	}
	
	private void updateMessage(){
		CAWClientGUIManager.getLastMessage().ifPresentOrElse(message::setMessage, () -> message.setMessage(Component.empty()));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}
	
	private void refresh(){
		this.copyFactionData();
		this.ensurePageNumberValid();
		this.populatePage();
		this.updateMessage();
	}
	
	@Override
	public void tick() {
		super.tick();
		this.refresh();
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
