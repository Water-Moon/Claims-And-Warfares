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

public class PTSRejectInvitationPacket {
	
	UUID invitationUUID;
	
	public PTSRejectInvitationPacket(UUID invitationUUID) {
		this.invitationUUID = invitationUUID;
	}
	
	public static PTSRejectInvitationPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID invitationUUID = byteBuf.readUUID();
		return new PTSRejectInvitationPacket(invitationUUID);
	}
	
	public static void toBytes(PTSRejectInvitationPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.invitationUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.declineInvite(player, invitationUUID).ifLeft(factionData -> {
				ChannelRegistry.sendToClient(player, new PTCRejectInvitationSuccessPacket(factionData.getFactionName()));
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
