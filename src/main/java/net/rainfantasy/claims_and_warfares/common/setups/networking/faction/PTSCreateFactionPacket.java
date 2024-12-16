package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericErrorPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.function.Supplier;

public class PTSCreateFactionPacket {
	
	String factionName;
	
	public PTSCreateFactionPacket(String factionName) {
		this.factionName = factionName;
	}
	
	public static PTSCreateFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		String factionName = byteBuf.readUtf();
		return new PTSCreateFactionPacket(factionName);
	}
	
	public static void toBytes(PTSCreateFactionPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		ServerPlayer player = context.getSender();
		if (player == null) {
			context.setPacketHandled(true);
			return;
		}
		CAWConstants.execute(() -> {
			MinecraftServer server = player.getServer();
			if (server == null) return;
			server.executeIfPossible(() -> {
				Either<FactionData, Component> result = FactionPacketHandler.createFaction(player, this.factionName);
				result.ifLeft(factionData -> {
					ChannelRegistry.sendToClient(player, new PTCFactionCreatedPacket(factionData.getName()));
					FactionPacketGenerator.scheduleSend(player);
				}).ifRight(component -> {
					ChannelRegistry.sendToClient(player, new PTCGenericErrorPacket(component));
				});
			});
		});
		context.setPacketHandled(true);
	}
}
