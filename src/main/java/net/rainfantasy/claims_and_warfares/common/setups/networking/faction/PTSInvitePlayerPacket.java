package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericErrorPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSInvitePlayerPacket {
	
	UUID toPlayer;
	UUID toFaction;
	
	public PTSInvitePlayerPacket(UUID toPlayer, UUID toFaction) {
		this.toPlayer = toPlayer;
		this.toFaction = toFaction;
	}
	
	public static PTSInvitePlayerPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID toPlayer = byteBuf.readUUID();
		UUID toFaction = byteBuf.readUUID();
		return new PTSInvitePlayerPacket(toPlayer, toFaction);
	}
	
	public static void toBytes(PTSInvitePlayerPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.toPlayer);
		byteBuf.writeUUID(packet.toFaction);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.invitePlayer(player, toPlayer, toFaction).ifLeft(info -> {
				ChannelRegistry.sendToClient(player, new PTCInvitationSuccessPacket(info));
				FactionPacketGenerator.scheduleSend(player);
			}).ifRight(error -> {
				ChannelRegistry.sendToClient(player, new PTCGenericErrorPacket(error));
			});
		});
		context.setPacketHandled(true);
	}
}