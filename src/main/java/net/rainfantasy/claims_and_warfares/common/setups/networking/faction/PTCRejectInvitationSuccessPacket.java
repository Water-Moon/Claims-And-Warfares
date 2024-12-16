package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCRejectInvitationSuccessPacket {
	
	String factionName;
	
	public PTCRejectInvitationSuccessPacket(String factionName) {
		this.factionName = factionName;
	}
	
	public static PTCRejectInvitationSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		String factionName = byteBuf.readUtf();
		return new PTCRejectInvitationSuccessPacket(factionName);
	}
	
	public static void toBytes(PTCRejectInvitationSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.displayMessage(Component.translatable("caw.message.faction.invite_declined.u", factionName));
		});
		context.setPacketHandled(true);
		
	}
}
