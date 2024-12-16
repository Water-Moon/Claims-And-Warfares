package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCTransferFactionSuccessPacket {
	String newOwnerName;
	String factionName;
	
	public PTCTransferFactionSuccessPacket(String newOwnerName, String factionName) {
		this.newOwnerName = newOwnerName;
		this.factionName = factionName;
	}
	
	public static PTCTransferFactionSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCTransferFactionSuccessPacket(byteBuf.readUtf(), byteBuf.readUtf());
	}
	
	public static void toBytes(PTCTransferFactionSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.newOwnerName);
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			Component message = Component.translatable("caw.message.faction.transfer_success", factionName, newOwnerName);
			CAWClientGUIManager.openFactionManagementPage();
			CAWClientGUIManager.setLastMessage(message);
			CAWClientGUIManager.displayMessage(message);
		});
		context.setPacketHandled(true);
	}
}
