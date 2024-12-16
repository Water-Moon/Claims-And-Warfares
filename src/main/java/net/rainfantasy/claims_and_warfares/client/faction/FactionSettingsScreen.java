package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSRenamePacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSSetColorPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSSetFakePlayerPolicyPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData.*;

public class FactionSettingsScreen extends Screen {
	
	
	StringWidget colorSelectionLabel;
	StringWidget colorPreview;
	Button[][] colors = new Button[4][4];
	
	StringWidget factionNameLabel;
	EditBox factionNameInput;
	Button setNameButton;
	
	Button FakePlayerPolicyButton;
	
	StringWidget message;
	
	Button cancelButton;
	
	String currentFactionName = "";
	
	public FactionSettingsScreen() {
		super(Component.literal("Faction Settings"));
	}
	
	@Override
	protected void init() {
		super.init();
		int x = this.width / 2;
		int y = this.height / 2;
		int xStart = x - 175;
		int yStart = y - 110;
		factionNameLabel = new StringWidget(xStart, yStart, 100, 20, Component.translatable("caw.gui.label.faction_name"), this.font);
		this.addRenderableWidget(factionNameLabel);
		
		factionNameInput = new EditBox(this.font, xStart + 100, yStart, 180, 20, Component.literal(""));
		this.addRenderableWidget(factionNameInput);
		
		setNameButton = Button.builder(Component.translatable("caw.gui.common.accept"), btn -> {
			String newName = factionNameInput.getValue();
			ChannelRegistry.sendToServer(new PTSRenamePacket(CAWClientDataManager.getCurrentSelectedFactionUUID(), newName));
		}).pos(xStart + 290, yStart).size(50, 20).build();
		this.addRenderableWidget(setNameButton);
		
		colorSelectionLabel = new StringWidget(xStart + 125, yStart + 30, 50, 20, Component.translatable("caw.gui.label.faction_color"), this.font);
		this.addRenderableWidget(colorSelectionLabel);
		
		colorPreview = new StringWidget(xStart + 175, yStart + 30, 50, 20, Component.translatable("caw.gui.common.current"), this.font);
		this.addRenderableWidget(colorPreview);
		
		int colorButtonsXStart = xStart + 125;
		int colorButtonsYStart = yStart + 50;
		int colorButtonsWidth = 20;
		int colorButtonsHeight = 20;
		int colorButtonsGap = 5;
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int color = i * 4 + j;
				colors[i][j] = Button.builder(
				Component.literal("█").withStyle(Optional.ofNullable(ChatFormatting.getById(color)).orElseThrow()),
				btn -> {
					int rgbColor = Optional.ofNullable(ChatFormatting.getById(color)).map(ChatFormatting::getColor).orElse(16777215);
					ChannelRegistry.sendToServer(new PTSSetColorPacket(CAWClientDataManager.getCurrentSelectedFactionUUID(), rgbColor));
				}).pos(colorButtonsXStart + i * (colorButtonsWidth + colorButtonsGap), colorButtonsYStart + j * (colorButtonsHeight + colorButtonsGap)).size(colorButtonsWidth, colorButtonsHeight).build();
				this.addRenderableWidget(colors[i][j]);
			}
		}
		
		FakePlayerPolicyButton = Button.builder(Component.translatable("caw.string.faction.fake_player_policy"), btn -> {
			int nextPolicy = switch (CAWClientDataManager.getCurrentSelectedFactionFakePlayerPolicy()) {
				case FAKE_PLAYER_POLICY_CHECK_UUID -> FAKE_PLAYER_POLICY_ALLOW;
				case FAKE_PLAYER_POLICY_ALLOW -> FAKE_PLAYER_POLICY_DENY;
				default -> FAKE_PLAYER_POLICY_CHECK_UUID;
			};
			ChannelRegistry.sendToServer(new PTSSetFakePlayerPolicyPacket(CAWClientDataManager.getCurrentSelectedFactionUUID(), nextPolicy));
		}).pos(xStart + 75, yStart + 160).size(200, 20).build();
		this.addRenderableWidget(FakePlayerPolicyButton);
		
		message = new StringWidget(xStart, yStart + 180, 350, 20, Component.empty(), this.font);
		this.addRenderableWidget(message);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.back"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(xStart + 50, yStart + 200).size(250, 20).build();
		this.addRenderableWidget(cancelButton);
	}
	
	@Override
	public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}
	
	private void refreshFactionName() {
		String factionName = CAWClientDataManager.getCurrentSelectedFactionName().getString();
		if (this.currentFactionName.equals(factionName)) return;
		this.factionNameInput.setValue(factionName);
		this.currentFactionName = factionName;
	}
	
	private void refreshCurrentFactionColor() {
		int color = CAWClientDataManager.getCurrentSelectedFactionColor();
		this.colorPreview.setMessage(Component.translatable("caw.gui.common.current")
		                             .withStyle(
		                             Style.EMPTY.withColor(color)
		                             ));
	}
	
	private void refreshFakePlayerPolicy() {
		int policy = CAWClientDataManager.getCurrentSelectedFactionFakePlayerPolicy();
		Component policyName = switch (policy) {
			case FAKE_PLAYER_POLICY_CHECK_UUID -> Component.translatable("caw.string.faction.fake_player.uuid");
			case FAKE_PLAYER_POLICY_ALLOW -> Component.translatable("caw.string.faction.fake_player.allow");
			case FAKE_PLAYER_POLICY_DENY -> Component.translatable("caw.string.faction.fake_player.deny");
			default -> Component.translatable("caw.gui.common.unknown");
		};
		this.FakePlayerPolicyButton.setMessage(
		Component.translatable("caw.string.faction.fake_player_policy", policyName)
		);
	}
	
	private void refreshMessage() {
		CAWClientGUIManager.getLastMessage().ifPresentOrElse(
		msg -> this.message.setMessage(msg),
		() -> this.message.setMessage(Component.empty())
		);
	}
	
	@Override
	public void tick() {
		super.tick();
		refreshFactionName();
		refreshCurrentFactionColor();
		refreshFakePlayerPolicy();
		refreshMessage();
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
