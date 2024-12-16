package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientPlayerData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.ServerPlayerInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PTCServerPlayerInfoPacket {
	
	List<ServerPlayerInfo> playerInfoList = new ArrayList<>();
	
	public PTCServerPlayerInfoPacket() {
	}
	
	public PTCServerPlayerInfoPacket(List<ServerPlayerInfo> playerInfoList) {
		this.playerInfoList = playerInfoList;
	}
	
	public static PTCServerPlayerInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		List<ServerPlayerInfo> playerInfoList = new ArrayList<>();
		int playerInfoListSize = byteBuf.readInt();
		for (int i = 0; i < playerInfoListSize; i++) {
			String playerName = byteBuf.readUtf();
			UUID playerUUID = byteBuf.readUUID();
			int factionListSize = byteBuf.readInt();
			List<UUID> factions = new ArrayList<>();
			for (int j = 0; j < factionListSize; j++) {
				factions.add(UUID.fromString(byteBuf.readUtf()));
			}
			playerInfoList.add(new ServerPlayerInfo(playerUUID, playerName, new HashSet<>(factions)));
		}
		return new PTCServerPlayerInfoPacket(playerInfoList);
	}
	
	public static void toBytes(PTCServerPlayerInfoPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeInt(packet.playerInfoList.size());
		for (ServerPlayerInfo playerInfo : packet.playerInfoList) {
			byteBuf.writeUtf(playerInfo.getPlayerName());
			byteBuf.writeUUID(playerInfo.getPlayerUUID());
			byteBuf.writeInt(playerInfo.getFactions().size());
			for (UUID faction : playerInfo.getFactions()) {
				byteBuf.writeUtf(faction.toString());
			}
		}
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			for (ServerPlayerInfo playerInfo : this.playerInfoList) {
				CAWClientDataManager.addPlayerData(
					new ClientPlayerData(
					playerInfo.getPlayerName(),
					playerInfo.getPlayerUUID(),
					playerInfo.getFactions())
				);
			}
		});
		context.setPacketHandled(true);
	}
}
