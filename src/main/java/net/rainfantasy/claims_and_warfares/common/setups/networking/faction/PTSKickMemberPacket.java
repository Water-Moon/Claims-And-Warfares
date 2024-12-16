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

public class PTSKickMemberPacket {
	
	UUID playerUUID;
	UUID factionUUID;
	
	public PTSKickMemberPacket(UUID playerUUID, UUID factionUUID) {
		this.playerUUID = playerUUID;
		this.factionUUID = factionUUID;
	}
	
	public static void toBytes(PTSKickMemberPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.playerUUID);
		byteBuf.writeUUID(packet.factionUUID);
	}
	
	public static PTSKickMemberPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSKickMemberPacket(byteBuf.readUUID(), byteBuf.readUUID());
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.kickPlayer(player, factionUUID, playerUUID)
			.ifLeft(name -> {
				ChannelRegistry.sendToClient(player, new PTCKickSuccessPacket(name));
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
