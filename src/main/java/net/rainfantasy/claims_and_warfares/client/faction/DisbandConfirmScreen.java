package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSDisbandFactionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

public class DisbandConfirmScreen extends AbstractYesNoScreen {
	
	public DisbandConfirmScreen() {
		super(
		Component.translatable("caw.gui.string.question.really_disband",
		CAWClientDataManager.getCurrentSelectedFactionName()
		),
		true
		);
	}
	
	@Override
	void onYes() {
		CAWClientDataManager.getCurrentSelectedFaction().ifPresent(
		faction -> ChannelRegistry.sendToServer(new PTSDisbandFactionPacket(faction.getFactionUUID()))
		);
	}
	
	@Override
	void onNo() {
		FactionManagementScreen.onSubPageCancel();
	}
}
