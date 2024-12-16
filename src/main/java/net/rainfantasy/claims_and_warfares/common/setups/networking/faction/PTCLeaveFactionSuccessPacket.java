package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.PTCOpenFactionManagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.function.Supplier;

public class PTCLeaveFactionSuccessPacket {
	String factionName;
	
	public PTCLeaveFactionSuccessPacket(String factionName) {
		this.factionName = factionName;
	}
	
	public static PTCLeaveFactionSuccessPacket fromBytes(FriendlyByteBuf byteBuf) {
		String factionName = byteBuf.readUtf();
		return new PTCLeaveFactionSuccessPacket(factionName);
	}
	
	public static void toBytes(PTCLeaveFactionSuccessPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.displayMessage(Component.translatable("caw.message.faction.leave.success", factionName));
		});
		context.setPacketHandled(true);
	}
}
