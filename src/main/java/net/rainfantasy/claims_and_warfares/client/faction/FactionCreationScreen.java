package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkDirection;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSCreateFactionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

public class FactionCreationScreen extends Screen {
	
	EditBox factionNameInput;
	StringWidget message;
	Button confirmButton;
	Button cancelButton;
	
	
	public FactionCreationScreen() {
		super(Component.literal("Create Faction"));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}
	
	@Override
	protected void init() {
		super.init();
		
		int lineWidth = 200;
		int lineHeight = 20;
		int x = (this.width - lineWidth) / 2;
		int lines = 5;
		int y = (this.height - lines * lineHeight) / 2;
		
		//Params: font, x, y, width, height, message
		factionNameInput = new EditBox(
		this.font,
		x,
		y,
		lineWidth,
		lineHeight,
		Component.literal("Faction Name")
		);
		this.addRenderableWidget(factionNameInput);
		
		StringWidget factionNameInputLabel = new StringWidget(
		x - 100,
		y,
		100,
		lineHeight,
		Component.translatable("caw.gui.label.faction_name"),
		this.font
		);
		this.addRenderableWidget(factionNameInputLabel);
		
		//parameter: x, y, width, height, message, font
		message = new StringWidget(
		x,
		y + lineHeight,
		lineWidth,
		lineHeight,
		Component.empty(),
		this.font
		);
		this.addRenderableWidget(message);
		
		confirmButton = Button.builder(Component.translatable("caw.gui.common.confirm"), btn -> {
			this.tryCreateFaction();
		}).pos(x, y + lineHeight * 4).size(75, lineHeight).build();
		this.addRenderableWidget(confirmButton);
		
		cancelButton = Button.builder(Component.translatable("caw.gui.common.cancel"), btn -> {
			FactionManagementScreen.onSubPageCancel();
		}).pos(x + 75 + 50, y + lineHeight * 4).size(75, lineHeight).build();
		this.addRenderableWidget(cancelButton);
	}
	
	@Override
	public void tick() {
		super.tick();
		CAWClientGUIManager.getLastMessage().ifPresent(message::setMessage);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	private void tryCreateFaction() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		String factionName = factionNameInput.getValue();
		ChannelRegistry.sendToServer(new PTSCreateFactionPacket(factionName));
	}
	
	
}
