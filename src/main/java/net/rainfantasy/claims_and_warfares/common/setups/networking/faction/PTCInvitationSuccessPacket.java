package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;

import java.util.function.Supplier;

public class PTCInvitationSuccessPacket {
	
	InvitationInfo info;
	
	public PTCInvitationSuccessPacket(InvitationInfo info) {
		this.info = info;
	}
	
	public static PTCInvitationSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		InvitationInfo info = new InvitationInfo().fromBytes(byteBuf);
		return new PTCInvitationSuccessPacket(info);
	}
	
	public static void toBytes(PTCInvitationSuccessPacket packet, FriendlyByteBuf byteBuf) {
		packet.info.toBytes(byteBuf);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			Component message = Component.translatable("caw.message.faction.invitation.sent", info.getToPlayerName(), info.getFactionName());
			CAWClientGUIManager.displayMessage(message);
			CAWClientGUIManager.setLastMessage(message);
		});
		context.setPacketHandled(true);
	}
}
