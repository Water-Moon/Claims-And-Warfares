package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;

import java.util.function.Supplier;

public class PTCInvitationCancelSuccessPacket {
	
	InvitationInfo info;
	
	public PTCInvitationCancelSuccessPacket(InvitationInfo info) {
		this.info = info;
	}
	
	public static PTCInvitationCancelSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		InvitationInfo info = new InvitationInfo().fromBytes(byteBuf);
		return new PTCInvitationCancelSuccessPacket(info);
	}
	
	public static void toBytes(PTCInvitationCancelSuccessPacket packet, FriendlyByteBuf byteBuf) {
		packet.info.toBytes(byteBuf);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			Component message = Component.translatable("caw.message.faction.invitation.withdrawn", info.getToPlayerName(), info.getFactionName());
			CAWClientGUIManager.displayMessage(message);
			CAWClientGUIManager.setLastMessage(message);
		});
		context.setPacketHandled(true);
	}
}
