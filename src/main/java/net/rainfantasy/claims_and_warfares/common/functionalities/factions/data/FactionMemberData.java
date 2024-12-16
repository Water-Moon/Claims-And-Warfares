package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;

import java.util.UUID;

public class FactionMemberData implements ISerializableNBTData<FactionMemberData, CompoundTag> {
	
	UUID playerUUID;
	String knownPlayerName = "";
	Boolean isOwner = false;
	Boolean isAdmin = false;
	
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	
	public String getKnownPlayerName() {
		return knownPlayerName;
	}
	
	public Boolean getOwner() {
		return isOwner;
	}
	
	public Boolean getAdmin() {
		return isAdmin;
	}
	
	public FactionMemberData() {
		this.playerUUID = UUID.randomUUID();
	}
	
	public FactionMemberData(UUID playerUUID, String knownPlayerName) {
		this.playerUUID = playerUUID;
		this.knownPlayerName = knownPlayerName;
	}
	
	FactionMemberData(UUID playerUUID, String knownPlayerName, Boolean isOwner, Boolean isAdmin) {
		this.playerUUID = playerUUID;
		this.knownPlayerName = knownPlayerName;
		this.isOwner = isOwner;
		this.isAdmin = isAdmin;
	}
	
	@Override
	public FactionMemberData readFromNBT(CompoundTag nbt) {
		this.playerUUID = UUID.fromString(nbt.getString("playerUUID"));
		this.knownPlayerName = nbt.getString("knownPlayerName");
		this.isOwner = nbt.getBoolean("isOwner");
		this.isAdmin = nbt.getBoolean("isAdmin");
		return this;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("playerUUID", playerUUID.toString());
		nbt.putString("knownPlayerName", knownPlayerName);
		nbt.putBoolean("isOwner", isOwner);
		nbt.putBoolean("isAdmin", isAdmin);
		return nbt;
	}
	
	@Override
	public String toString() {
		return "FactionMemberData{" +
		       "playerUUID=" + playerUUID +
		       ", knownPlayerName='" + knownPlayerName + '\'' +
		       ", isOwner=" + isOwner +
		       ", isAdmin=" + isAdmin +
		       '}';
	}
}
