package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSSetRelationshipPacket {
	
	UUID factionUUID;
	int relationship;
	
	public PTSSetRelationshipPacket(UUID factionUUID, int relationship) {
		this.factionUUID = factionUUID;
		this.relationship = relationship;
	}
	
	public static PTSSetRelationshipPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		int relationship = byteBuf.readVarInt();
		return new PTSSetRelationshipPacket(factionUUID, relationship);
	}
	
	public static void toBytes(PTSSetRelationshipPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeVarInt(packet.relationship);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.setDiplomaticRelationship(player, factionUUID, relationship)
			.ifLeft(data -> {
				ChannelRegistry.sendToClient(player, new PTCSetRelationshipSuccessPacket(data.getFirst(), data.getSecond()));
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
