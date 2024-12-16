package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCDisbandSuccessPacket {
	
	String factionName;
	
	public PTCDisbandSuccessPacket(String factionName) {
		this.factionName = factionName;
	}
	
	public static PTCDisbandSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCDisbandSuccessPacket(byteBuf.readUtf());
	}
	
	public static void toBytes(PTCDisbandSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.openFactionManagementPage();
			CAWClientGUIManager.displayMessage(
			Component.translatable("caw.message.faction.disband_success", factionName)
			);
		});
		context.setPacketHandled(true);
	}
	
}
