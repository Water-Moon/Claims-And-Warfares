package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericMessagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

import static net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData.*;

public class PTSSetColorPacket {
	int color;
	UUID factionUUID;
	
	public PTSSetColorPacket(UUID factionUUID, int color) {
		this.factionUUID = factionUUID;
		this.color = color;
	}
	
	public static PTSSetColorPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		int relationship = byteBuf.readVarInt();
		return new PTSSetColorPacket(factionUUID, relationship);
	}
	
	public static void toBytes(PTSSetColorPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeVarInt(packet.color);
	}
	
	public void execute(Supplier<Context> supplier){
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.setColor(player, factionUUID, color)
			.ifLeft(data -> {
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
