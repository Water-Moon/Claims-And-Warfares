package net.rainfantasy.claims_and_warfares.client.data_types;

import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientFactionData {
	
	final UUID factionUUID;
	final String factionName;
	final FactionPlayerData[] players;
	final int color;
	final int fakePlayerPolicy;
	
	public ClientFactionData(FactionInfo infoIn) {
		factionUUID = infoIn.getFactionUUID();
		factionName = infoIn.getFactionName();
		players = new FactionPlayerData[infoIn.getMembers().size()];
		int i = 0;
		for (UUID playerUUID : infoIn.getMembers()) {
			FactionPlayerData playerData = new FactionPlayerData(playerUUID, playerUUID.equals(infoIn.getOwner()), infoIn.getAdmins().contains(playerUUID));
			players[i] = playerData;
			i++;
		}
		color = infoIn.getColor();
		fakePlayerPolicy = infoIn.getFakePlayerPolicy();
	}
	
	public UUID getFactionUUID() {
		return factionUUID;
	}
	
	public String getFactionName() {
		return factionName;
	}
	
	public int getColor() {
		return color;
	}
	
	public Set<UUID> getMembers() {
		return Arrays.stream(players).map(FactionPlayerData::playerUUID).collect(Collectors.toSet());
	}
	
	public Set<UUID> getAdmins() {
		return Arrays.stream(players).filter(FactionPlayerData::isAdmin).map(FactionPlayerData::playerUUID).collect(Collectors.toSet());
	}
	
	public UUID getOwner() {
		return Arrays.stream(players).filter(FactionPlayerData::isOwner).map(FactionPlayerData::playerUUID).findFirst().orElse(null);
	}
	
	public boolean isPlayerInFaction(UUID playerUUID) {
		return Arrays.stream(players).anyMatch(playerData -> playerData.playerUUID().equals(playerUUID));
	}
	
	public boolean isPlayerAdmin(UUID playerUUID) {
		return Arrays.stream(players).anyMatch(playerData -> playerData.playerUUID().equals(playerUUID) && playerData.isAdmin());
	}
	
	public boolean isPlayerOwner(UUID playerUUID) {
		return Arrays.stream(players).anyMatch(playerData -> playerData.playerUUID().equals(playerUUID) && playerData.isOwner());
	}
	
	public boolean isPlayerAdminOrOwner(UUID playerUUID) {
		return isPlayerAdmin(playerUUID) || isPlayerOwner(playerUUID);
	}
	
	public int getFakePlayerPolicy() {
		return fakePlayerPolicy;
	}
	
	@Override
	public String toString() {
		return "ClientFactionData{" +
		       "factionUUID=" + factionUUID +
		       ", factionName='" + factionName + '\'' +
		       ", players=" + Arrays.toString(players) +
		       '}';
	}
}

record FactionPlayerData(UUID playerUUID, boolean isOwner, boolean isAdmin) {
	
	@Override
	public String toString() {
		return "FactionPlayerData{" +
		       "playerUUID=" + playerUUID +
		       ", isOwner=" + isOwner +
		       ", isAdmin=" + isAdmin +
		       '}';
	}
	
	@Override
	public UUID playerUUID() {
		return playerUUID;
	}
	
	@Override
	public boolean isOwner() {
		return isOwner;
	}
	
	@Override
	public boolean isAdmin() {
		return isAdmin;
	}
}
