package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.PTSOpenTransferFactionConfirmPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class TransferFactionScreen extends Screen {
	
	private static final int PLAYERS_PER_PAGE = 5;
	private static final int LINE_HEIGHT = 20;
	private static final int NAME_WIDTH = 150;
	private static final int BUTTON_WIDTH = 50;
	private static final int TOTAL_WIDTH = NAME_WIDTH + BUTTON_WIDTH;
	
	private static final int PAGE_BUTTON_WIDTH = 100;
	
	private static final int TOTAL_LINES = PLAYERS_PER_PAGE + 3;
	
	StringWidget[] playerNames = new StringWidget[PLAYERS_PER_PAGE];
	StringWidget message;
	Button[] transferButtons = new Button[PLAYERS_PER_PAGE];
	
	Button nextPageButton;
	Button previousPageButton;
	Button cancelButton;
	
	int currentPage = 1;
	List<UUID> players = new ArrayList<>();
	UUID[] currentPagePlayers = new UUID[PLAYERS_PER_PAGE];
	
	public TransferFactionScreen() {
		super(Component.literal("Member Management"));
	}
	
	@SuppressWarnings("DuplicatedCode")
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int entryLineXStart = xCenter - TOTAL_WIDTH / 2;
		int yStart = yCenter - (LINE_HEIGHT * TOTAL_LINES) / 2;
		
		for (int i = 0; i < PLAYERS_PER_PAGE; i++) {
			playerNames[i] = new StringWidget(entryLineXStart, yStart + LINE_HEIGHT * i, NAME_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
			addRenderableWidget(playerNames[i]);
			
			int finalI = i;
			transferButtons[i] = Button.builder(Component.translatable("caw.gui.button.transfer"), btn -> {
				onTransferButtonPressed(finalI);
			}).pos(entryLineXStart + NAME_WIDTH, yStart + LINE_HEIGHT * i).size(BUTTON_WIDTH, LINE_HEIGHT).build();
			addRenderableWidget(transferButtons[i]);
		}
		
		message = new StringWidget(xCenter - 100, yStart + LINE_HEIGHT * PLAYERS_PER_PAGE, 200, LINE_HEIGHT, Component.empty(), this.font);
		addRenderableWidget(message);
		
		previousPageButton = Button.builder(Component.translatable("caw.gui.common.prev_page"), btn -> {
			this.changePage(-1);
		}).pos(xCenter - PAGE_BUTTON_WIDTH - 50, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 1)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		nextPageButton = Button.builder(Component.translatable("caw.gui.common.next_page"), btn -> {
			this.changePage(1);
		}).pos(xCenter + 50, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 1)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(previousPageButton);
		addRenderableWidget(nextPageButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.back"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xCenter - PAGE_BUTTON_WIDTH / 2, yStart + LINE_HEIGHT * (PLAYERS_PER_PAGE + 2)).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(cancelButton);
	}
	
	private void onTransferButtonPressed(int index) {
		CAWConstants.debugLog("Transfer button {} pressed", index);
		UUID playerUUID = currentPagePlayers[index];
		UUID currentFactionUUID = CAWClientDataManager.getCurrentSelectedFaction().map(ClientFactionData::getFactionUUID).orElse(null);
		ChannelRegistry.sendToServer(new PTSOpenTransferFactionConfirmPacket(currentFactionUUID, playerUUID));
	}
	
	private void copyPlayerData() {
		players.clear();
		CAWClientDataManager.getPlayersInCurrentSelectedFaction().ifPresent(players::addAll);
	}
	
	private void updateMessage() {
		CAWClientGUIManager.getLastMessage().ifPresentOrElse(
		message::setMessage,
		() -> message.setMessage(Component.empty())
		);
	}
	
	private void setupPlayerEntry(int index, int listIndex) {
		currentPagePlayers[index] = players.get(listIndex);
		boolean isSelfOwner = CAWClientDataManager.isSelfOwnerOfCurrentFaction();
		boolean isSelf = currentPagePlayers[index].equals(CAWClientDataManager.getClientPlayerUUID().orElse(null));
		
		MutableComponent playerName = CAWClientDataManager.getPlayerNameIfKnown(currentPagePlayers[listIndex])
		                              .map(Component::literal)
		                              .orElse(Component.translatable("caw.gui.label.empty"));
		if (isSelf) {
			playerName.append(Component.translatable("caw.gui.format.bracket",
			Component.translatable("caw.gui.common.self")));
		}
		if (CAWClientDataManager.isLikelyOffline(currentPagePlayers[listIndex])) {
			playerName.append(Component.translatable("caw.gui.format.bracket",
			Component.translatable("caw.gui.common.offline")));
		}
		
		playerNames[index].setMessage(playerName);
		
		transferButtons[index].active = isSelfOwner && !isSelf;
	}
	
	private void setupEmptyEntry(int index) {
		playerNames[index].setMessage(Component.translatable("caw.gui.label.empty"));
		transferButtons[index].active = false;
	}
	
	private void changePage(int delta) {
		currentPage += delta;
		ensurePageNumberValid();
		populateCurrentPage();
	}
	
	private void ensurePageNumberValid() {
		if (currentPage < 1) {
			currentPage = 1;
		}
		int maxPage = Math.max(1, (int) Math.ceil((double) players.size() / PLAYERS_PER_PAGE));
		if (currentPage > maxPage) {
			currentPage = maxPage;
		}
	}
	
	private void populateCurrentPage() {
		for (int i = 0; i < PLAYERS_PER_PAGE; i++) {
			int listIndex = (currentPage - 1) * PLAYERS_PER_PAGE + i;
			if (listIndex < players.size()) {
				setupPlayerEntry(i, listIndex);
			} else {
				setupEmptyEntry(i);
			}
		}
	}
	
	private void refresh() {
		copyPlayerData();
		ensurePageNumberValid();
		populateCurrentPage();
		updateMessage();
	}
	
	@Override
	public void tick() {
		super.tick();
		refresh();
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
