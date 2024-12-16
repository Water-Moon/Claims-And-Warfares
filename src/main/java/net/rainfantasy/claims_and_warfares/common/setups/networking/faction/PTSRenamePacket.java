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

public class PTSRenamePacket {
	String newName;
	UUID factionUUID;
	
	public PTSRenamePacket(UUID factionUUID, String newName) {
		this.factionUUID = factionUUID;
		this.newName = newName;
	}
	
	public static PTSRenamePacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		String relationship = byteBuf.readUtf();
		return new PTSRenamePacket(factionUUID, relationship);
	}
	
	public static void toBytes(PTSRenamePacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeUtf(packet.newName);
	}
	
	public void execute(Supplier<Context> supplier){
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.renameFaction(player, factionUUID, newName)
			.ifLeft(data -> {
				ChannelRegistry.sendToClient(player, new PTCGenericMessagePacket(
					Component.translatable("caw.message.faction.faction_renamed_success", data.getFirst(), data.getSecond())
				));
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
