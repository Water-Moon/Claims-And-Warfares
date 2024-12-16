package net.rainfantasy.claims_and_warfares.client.faction;

import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTSTransferFactionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;

public class TransferFactionConfirmScreen extends AbstractYesNoScreen {
	
	public static TransferFactionConfirmScreen MakeTransferFactionScreen(UUID factionUUID, UUID newOwnerUUID) {
		Component factionName = CAWClientDataManager.getFactionData(factionUUID)
		                        .map(ClientFactionData::getFactionName)
		                        .map(Component::literal)
		                        .orElse(Component.translatable("caw.gui.common.unknown"));
		Component ownerName = CAWClientDataManager.getPlayerNameIfKnown(newOwnerUUID)
		                      .map(Component::literal)
		                      .orElse(Component.translatable("caw.gui.common.unknown"));
		return new TransferFactionConfirmScreen(factionUUID, newOwnerUUID, factionName, ownerName);
	}
	
	UUID factionUUID;
	UUID newOwnerUUID;
	
	private TransferFactionConfirmScreen(UUID factionUUID, UUID newOwnerUUID, Component factionName, Component ownerName) {
		super(Component.translatable("caw.gui.string.question.really_transfer", factionName, ownerName), true);
		this.factionUUID = factionUUID;
		this.newOwnerUUID = newOwnerUUID;
	}
	
	@Override
	void onYes() {
		ChannelRegistry.sendToServer(new PTSTransferFactionPacket(factionUUID, newOwnerUUID));
	}
	
	@Override
	void onNo() {
		FactionManagementScreen.onSubPageCancel();
	}
}
