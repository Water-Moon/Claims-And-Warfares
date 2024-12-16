package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSLeaveFactionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

public class LeaveFactionConfirmScreen extends AbstractYesNoScreen{
	
	
	public LeaveFactionConfirmScreen() {
		super(Component.translatable(
			"caw.gui.string.question.really_leave",
			CAWClientDataManager.getCurrentSelectedFactionName()
		), true);
	}
	
	@Override
	void onYes() {
		CAWClientDataManager.getCurrentSelectedFaction().ifPresent(factionData -> {
			ChannelRegistry.sendToServer(new PTSLeaveFactionPacket(factionData.getFactionUUID()));
		});
	}
	
	@Override
	void onNo() {
		FactionManagementScreen.onSubPageCancel();
	}
}
