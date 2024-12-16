package net.rainfantasy.claims_and_warfares.client.data_types;

import java.util.Set;
import java.util.UUID;

public class ClientPlayerData {
	
	String playerName;
	UUID playerUUID;
	Set<UUID> factions;
	
	public ClientPlayerData(String playerName, UUID playerUUID, Set<UUID> factions) {
		this.playerName = playerName;
		this.playerUUID = playerUUID;
		this.factions = Set.copyOf(factions);
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	
	public Set<UUID> getFactions() {
		return factions;
	}
	
	@Override
	public String toString() {
		return "ClientPlayerData{" +
		       "playerName='" + playerName + '\'' +
		       ", playerUUID=" + playerUUID +
		       ", factions=" + factions +
		       '}';
	}
}
