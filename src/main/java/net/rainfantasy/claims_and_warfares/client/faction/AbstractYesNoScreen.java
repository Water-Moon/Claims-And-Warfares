package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractYesNoScreen extends Screen {
	
	StringWidget message;
	StringWidget cantUndoMessage;
	Button yesButton;
	Button noButton;
	StringWidget displayMessage;
	Component question;
	boolean cantUndo;
	
	public AbstractYesNoScreen(Component question, boolean isCantUndo) {
		super(Component.literal("Yes or No"));
		this.question = question;
		this.cantUndo = isCantUndo;
	}
	
	int getMessageLength() {
		return 200;
	}
	
	@Override
	protected void init() {
		super.init();
		
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		
		int yStart = yCenter - 50;
		int xStart = xCenter - getMessageLength() / 2;
		
		message = new StringWidget(xStart, yStart, getMessageLength(), yCenter - 50, this.question, this.font);
		addRenderableWidget(message);
		
		if (cantUndo) {
			cantUndoMessage = new StringWidget(xStart, yStart, getMessageLength(), yCenter - 30, Component.translatable("caw.gui.common.no_undo"), this.font);
			addRenderableWidget(cantUndoMessage);
		}
		
		displayMessage = new StringWidget(xStart, yStart, getMessageLength(), yCenter + 10, Component.empty(), this.font);
		addRenderableWidget(displayMessage);
		
		yesButton = Button.builder(Component.translatable("caw.gui.common.yes"), btn -> onYes())
		            .size(50, 20).pos(xCenter - 75, yCenter + 30).build();
		addRenderableWidget(yesButton);
		noButton = Button.builder(Component.translatable("caw.gui.common.no"), btn -> onNo())
		           .size(50, 20).pos(xCenter + 25, yCenter + 30).build();
		addRenderableWidget(noButton);
	}
	
	@Override
	public void render(@NotNull GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
		super.renderBackground(p_281549_);
		super.render(p_281549_, p_281550_, p_282878_, p_282465_);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		CAWClientGUIManager.getLastMessage().ifPresent(message -> {
			displayMessage.setMessage(message);
		});
	}
	
	abstract void onYes();
	
	abstract void onNo();
}
