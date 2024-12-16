package net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.INetworkInfo;

import java.util.Set;
import java.util.UUID;

public class ServerPlayerInfo implements INetworkInfo<ServerPlayerInfo> {
	
	UUID playerUUID;
	String playerName;
	Set<UUID> factions;
	
	public ServerPlayerInfo() {
	}
	
	public ServerPlayerInfo(UUID playerUUID, String playerName, Set<UUID> factions) {
		this.playerUUID = playerUUID;
		this.playerName = playerName;
		this.factions = factions;
	}
	
	@Override
	public void toBytes(FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(playerUUID);
		byteBuf.writeUtf(playerName);
		byteBuf.writeVarInt(factions.size());
		for (UUID factionUUID : factions) {
			byteBuf.writeUUID(factionUUID);
		}
	}
	
	@Override
	public ServerPlayerInfo fromBytes(FriendlyByteBuf byteBuf) {
		UUID playerUUID = byteBuf.readUUID();
		String playerName = byteBuf.readUtf();
		int factionCount = byteBuf.readVarInt();
		for (int i = 0; i < factionCount; i++) {
			UUID factionUUID = byteBuf.readUUID();
			factions.add(factionUUID);
		}
		return new ServerPlayerInfo(playerUUID, playerName, factions);
	}
	
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public Set<UUID> getFactions() {
		return factions;
	}
}
