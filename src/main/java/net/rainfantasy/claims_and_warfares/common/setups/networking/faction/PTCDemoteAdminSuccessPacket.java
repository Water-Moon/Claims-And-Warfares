package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCDemoteAdminSuccessPacket {
	
	String playerName;
	
	public PTCDemoteAdminSuccessPacket(String playerName) {
		this.playerName = playerName;
	}
	
	public static void toBytes(PTCDemoteAdminSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.playerName);
	}
	
	public static PTCDemoteAdminSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		String playerName = byteBuf.readUtf();
		return new PTCDemoteAdminSuccessPacket(playerName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.setLastMessage(Component.translatable("caw.message.faction.demoted_player_admin", playerName));
			CAWClientGUIManager.displayMessage(Component.translatable("caw.message.faction.demoted_player_admin", playerName));
		});
		context.setPacketHandled(true);
	}
}
