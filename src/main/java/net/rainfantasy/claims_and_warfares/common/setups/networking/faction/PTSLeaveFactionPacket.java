package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.PTCOpenFactionManagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSLeaveFactionPacket {
	
	UUID factionUUID;
	
	public PTSLeaveFactionPacket(UUID factionUUID) {
		this.factionUUID = factionUUID;
	}
	
	public static PTSLeaveFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		return new PTSLeaveFactionPacket(factionUUID);
	}
	
	public static void toBytes(PTSLeaveFactionPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.leaveFaction(player, factionUUID)
			.ifLeft(factionData -> {
				ChannelRegistry.sendToClient(player, new PTCLeaveFactionSuccessPacket(factionData.getFactionName()));
				ChannelRegistry.sendToClient(player, new PTCOpenFactionManagePacket());
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
