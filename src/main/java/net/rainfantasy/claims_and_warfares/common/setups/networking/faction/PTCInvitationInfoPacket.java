package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;

import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class PTCInvitationInfoPacket {
	private final InvitationInfo info;
	
	public PTCInvitationInfoPacket(InvitationInfo info) {
		this.info = info;
	}
	
	public InvitationInfo getInfo() {
		return info;
	}
	
	public static PTCInvitationInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		InvitationInfo info = new InvitationInfo().fromBytes(byteBuf);
		return new PTCInvitationInfoPacket(info);
	}
	
	public static void toBytes(PTCInvitationInfoPacket packet, FriendlyByteBuf byteBuf) {
		packet.info.toBytes(byteBuf);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientDataManager.addInvitation(this.info);
		});
		context.setPacketHandled(true);
	}
	
	
	
	
}
