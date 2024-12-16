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

public class PTSDisbandFactionPacket {
	
	UUID factionUUID;
	
	public PTSDisbandFactionPacket(UUID factionUUID) {
		this.factionUUID = factionUUID;
	}
	
	public static PTSDisbandFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSDisbandFactionPacket(byteBuf.readUUID());
	}
	
	public static void toBytes(PTSDisbandFactionPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.disband(player, factionUUID)
			.ifLeft(name -> {
				ChannelRegistry.sendToClient(player, new PTCDisbandSuccessPacket(name));
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
