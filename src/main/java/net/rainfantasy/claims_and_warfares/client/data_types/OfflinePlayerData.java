package net.rainfantasy.claims_and_warfares.client.data_types;

import java.util.UUID;

public class OfflinePlayerData {
	
	UUID playerUUID;
	String playerName;
	
	public OfflinePlayerData(UUID playerUUID, String playerName) {
		this.playerUUID = playerUUID;
		this.playerName = playerName;
	}
	
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
}
