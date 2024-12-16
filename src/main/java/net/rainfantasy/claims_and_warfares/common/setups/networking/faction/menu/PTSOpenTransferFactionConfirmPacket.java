package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSOpenTransferFactionConfirmPacket {
	
	UUID factionUUID;
	UUID playerUUID;
	
	public PTSOpenTransferFactionConfirmPacket(UUID factionUUID, UUID playerUUID) {
		this.factionUUID = factionUUID;
		this.playerUUID = playerUUID;
	}
	
	public static PTSOpenTransferFactionConfirmPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSOpenTransferFactionConfirmPacket(byteBuf.readUUID(), byteBuf.readUUID());
	}
	
	public static void toBytes(PTSOpenTransferFactionConfirmPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeUUID(packet.playerUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ChannelRegistry.reply(context, new PTCOpenTransferFactionConfirmPacket(factionUUID, playerUUID));
			FactionPacketGenerator.scheduleSend(context.getSender());
		});
		context.setPacketHandled(true);
	}
	
}
