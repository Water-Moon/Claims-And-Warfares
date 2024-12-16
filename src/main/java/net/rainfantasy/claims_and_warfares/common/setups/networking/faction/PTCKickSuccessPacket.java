package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCKickSuccessPacket {
	String playerName;
	
	public PTCKickSuccessPacket(String playerName) {
		this.playerName = playerName;
	}
	
	public static void toBytes(PTCKickSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.playerName);
	}
	
	public static PTCKickSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCKickSuccessPacket(byteBuf.readUtf());
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.displayMessage(Component.translatable("caw.message.faction.kick_success", playerName));
			CAWClientGUIManager.setLastMessage(Component.translatable("caw.message.faction.kick_success", playerName));
		});
		context.setPacketHandled(true);
	}
}
