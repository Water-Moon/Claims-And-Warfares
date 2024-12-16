package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSTransferFactionPacket {
	UUID factionUUID;
	UUID playerUUID;
	
	public PTSTransferFactionPacket(UUID factionUUID, UUID playerUUID) {
		this.factionUUID = factionUUID;
		this.playerUUID = playerUUID;
	}
	
	public static PTSTransferFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSTransferFactionPacket(byteBuf.readUUID(), byteBuf.readUUID());
	}
	
	public static void toBytes(PTSTransferFactionPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeUUID(packet.playerUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if(player == null) return;
			FactionPacketHandler.assignNewOwner(player, factionUUID, playerUUID)
			.ifLeft(data -> {
				ChannelRegistry.reply(context, new PTCTransferFactionSuccessPacket(data.getB().getKnownPlayerName(), data.getA().getFactionName()));
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
