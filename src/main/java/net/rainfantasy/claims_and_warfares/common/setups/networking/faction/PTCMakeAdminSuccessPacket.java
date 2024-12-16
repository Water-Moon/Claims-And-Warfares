package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCMakeAdminSuccessPacket {
	
	String playerName;
	
	public PTCMakeAdminSuccessPacket(String playerName) {
		this.playerName = playerName;
	}
	
	public static void toBytes(PTCMakeAdminSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.playerName);
	}
	
	public static PTCMakeAdminSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		String playerName = byteBuf.readUtf();
		return new PTCMakeAdminSuccessPacket(playerName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.setLastMessage(Component.translatable("caw.message.faction.made_player_admin", playerName));
			CAWClientGUIManager.displayMessage(Component.translatable("caw.message.faction.made_player_admin", playerName));
		});
		context.setPacketHandled(true);
	}
}
