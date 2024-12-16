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
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSSetRelationshipPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RelationshipManagementScreen extends Screen {
	
	private static final int ENTRIES_PER_PAGE = 5;
	private static final int LINE_HEIGHT = 20;
	private static final int NAME_WIDTH = 150;
	private static final int BUTTON_WIDTH = 50;
	private static final int BUTTON_GAP = 10;
	private static final int TOTAL_WIDTH = NAME_WIDTH + BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
	
	private static final int PAGE_BUTTON_WIDTH = 100;
	
	private static final int TOTAL_LINES = ENTRIES_PER_PAGE + 3;
	
	StringWidget[] factionNames = new StringWidget[ENTRIES_PER_PAGE];
	StringWidget message;
	Button[] allyButtons = new Button[ENTRIES_PER_PAGE];
	Button[] neutralButtons = new Button[ENTRIES_PER_PAGE];
	Button[] enemyButtons = new Button[ENTRIES_PER_PAGE];
	
	Button nextPageButton;
	Button previousPageButton;
	Button cancelButton;
	
	int currentPage = 1;
	List<UUID> factions = new ArrayList<>();
	UUID[] currentPageFactions = new UUID[ENTRIES_PER_PAGE];
	
	public RelationshipManagementScreen() {
		super(Component.literal("Relationship Management"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		int entryLineXStart = xCenter - TOTAL_WIDTH / 2;
		int yStart = yCenter - TOTAL_LINES * LINE_HEIGHT / 2;
		
		for (int i = 0; i < ENTRIES_PER_PAGE; i++) {
			factionNames[i] = new StringWidget(entryLineXStart, yStart + i * LINE_HEIGHT, NAME_WIDTH, LINE_HEIGHT, Component.translatable("caw.gui.label.empty"), this.font);
			addRenderableWidget(factionNames[i]);
			
			int finalI = i;
			allyButtons[i] = Button.builder(Component.translatable("caw.string.faction.ally"), btn -> {
				onButtonPressed(finalI, 1);
			}).pos(entryLineXStart + NAME_WIDTH, yStart + i * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
			addRenderableWidget(allyButtons[i]);
			
			neutralButtons[i] = Button.builder(Component.translatable("caw.string.faction.neutral"), btn -> {
				onButtonPressed(finalI, 0);
			}).pos(entryLineXStart + NAME_WIDTH + BUTTON_WIDTH + BUTTON_GAP, yStart + i * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
			addRenderableWidget(neutralButtons[i]);
			
			enemyButtons[i] = Button.builder(Component.translatable("caw.string.faction.hostile"), btn -> {
				onButtonPressed(finalI, -1);
			}).pos(entryLineXStart + NAME_WIDTH + BUTTON_WIDTH * 2 + BUTTON_GAP * 2, yStart + i * LINE_HEIGHT).size(BUTTON_WIDTH, LINE_HEIGHT).build();
			addRenderableWidget(enemyButtons[i]);
		}
		
		message = new StringWidget(xCenter - TOTAL_WIDTH / 2, yStart + ENTRIES_PER_PAGE * LINE_HEIGHT, TOTAL_WIDTH, LINE_HEIGHT, Component.empty(), this.font);
		addRenderableWidget(message);
		
		nextPageButton = Button.builder(Component.translatable("caw.gui.common.next_page"), btn -> {
			changePage(1);
		}).pos(xCenter + 50, yStart + (1 + ENTRIES_PER_PAGE) * LINE_HEIGHT).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(nextPageButton);
		
		previousPageButton = Button.builder(Component.translatable("caw.gui.common.prev_page"), btn -> {
			changePage(-1);
		}).pos(xCenter - PAGE_BUTTON_WIDTH - 50, yStart + (1 + ENTRIES_PER_PAGE) * LINE_HEIGHT).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(previousPageButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.cancel"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xCenter - PAGE_BUTTON_WIDTH / 2, yStart + (2 + ENTRIES_PER_PAGE) * LINE_HEIGHT).size(PAGE_BUTTON_WIDTH, LINE_HEIGHT).build();
		addRenderableWidget(cancelButton);
	}
	
	private void onButtonPressed(int index, int level) {
		UUID factionUUID = currentPageFactions[index];
		CAWConstants.debugLog("Button pressed for faction " + factionUUID + " with level " + level);
		ChannelRegistry.sendToServer(new PTSSetRelationshipPacket(factionUUID, level));
	}
	
	private void copyFactionData() {
		factions.clear();
		factions.addAll(CAWClientDataManager.getFactionUUIDs());
	}
	
	private void updateMessage() {
		CAWClientGUIManager.getLastMessage().ifPresentOrElse(
		msg -> message.setMessage(msg),
		() -> message.setMessage(Component.empty())
		);
	}
	
	private void setupValidEntry(int index, int listIndex) {
		currentPageFactions[index] = factions.get(listIndex);
		boolean isSelf = CAWClientDataManager.getCurrentSelectedFactionUUID().equals(currentPageFactions[index]);
		int existingRelationship = CAWClientDataManager.getDiplomaticRelationshipWith(currentPageFactions[index]);
		
		MutableComponent factionName = CAWClientDataManager.getFactionData(currentPageFactions[index])
		                               .map(ClientFactionData::getFactionName)
		                               .map(Component::literal)
		                               .orElse(Component.translatable("caw.gui.label.empty"));
		
		if (isSelf) {
			factionName.append(Component.translatable("caw.gui.format.bracket", Component.translatable("caw.gui.label.your_faction")));
		}
		
		factionNames[index].setMessage(factionName);
		
		allyButtons[index].active = !isSelf && existingRelationship != 1;
		neutralButtons[index].active = !isSelf && existingRelationship != 0;
		enemyButtons[index].active = !isSelf && existingRelationship != -1;
	}
	
	private void setupEmptyEntry(int index) {
		factionNames[index].setMessage(Component.translatable("caw.gui.label.empty"));
		allyButtons[index].active = false;
		neutralButtons[index].active = false;
		enemyButtons[index].active = false;
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
		int maxPage = Math.max(1, (int) Math.ceil((double) factions.size() / ENTRIES_PER_PAGE));
		if (currentPage > maxPage) {
			currentPage = maxPage;
		}
	}
	
	private void populateCurrentPage() {
		for (int i = 0; i < ENTRIES_PER_PAGE; i++) {
			int listIndex = (currentPage - 1) * ENTRIES_PER_PAGE + i;
			if (listIndex < factions.size()) {
				setupValidEntry(i, listIndex);
			} else {
				setupEmptyEntry(i);
			}
		}
	}
	
	private void refresh() {
		copyFactionData();
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
	public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
