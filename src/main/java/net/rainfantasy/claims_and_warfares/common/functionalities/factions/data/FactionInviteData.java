package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.SerializableDateTime;

import java.util.UUID;

public class FactionInviteData implements ISerializableNBTData<FactionInviteData, CompoundTag> {
	
	UUID fromPlayerUUID;
	String fromPlayerName;
	UUID toPlayerUUID;
	String toPlayerName;
	UUID factionUUID;
	SerializableDateTime sentTime;
	UUID invitationUUID = UUID.randomUUID();
	
	public FactionInviteData() {
	}
	
	public FactionInviteData(UUID fromPlayerUUID, String fromPlayerName, UUID toPlayerUUID, String toPlayerName, UUID factionUUID, SerializableDateTime sentTime) {
		this.fromPlayerUUID = fromPlayerUUID;
		this.fromPlayerName = fromPlayerName;
		this.toPlayerUUID = toPlayerUUID;
		this.toPlayerName = toPlayerName;
		this.factionUUID = factionUUID;
		this.sentTime = sentTime;
	}
	
	@Override
	public FactionInviteData readFromNBT(CompoundTag nbt) {
		this.fromPlayerUUID = UUID.fromString(nbt.getString("fromPlayerUUID"));
		this.fromPlayerName = nbt.getString("fromPlayerName");
		this.toPlayerUUID = UUID.fromString(nbt.getString("toPlayerUUID"));
		this.toPlayerName = nbt.getString("toPlayerName");
		this.factionUUID = UUID.fromString(nbt.getString("factionUUID"));
		this.sentTime = new SerializableDateTime().readFromNBT(nbt.getCompound("sentTime"));
		this.invitationUUID = UUID.fromString(nbt.getString("invitationUUID"));
		return this;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("fromPlayerUUID", fromPlayerUUID.toString());
		nbt.putString("fromPlayerName", fromPlayerName);
		nbt.putString("toPlayerUUID", toPlayerUUID.toString());
		nbt.putString("toPlayerName", toPlayerName);
		nbt.putString("factionUUID", factionUUID.toString());
		nbt.put("sentTime", sentTime.writeToNBT(new CompoundTag()));
		nbt.putString("invitationUUID", invitationUUID.toString());
		return nbt;
	}
	
	public UUID getFromPlayerUUID() {
		return fromPlayerUUID;
	}
	
	public String getFromPlayerName() {
		return fromPlayerName;
	}
	
	public UUID getToPlayerUUID() {
		return toPlayerUUID;
	}
	
	public String getToPlayerName() {
		return toPlayerName;
	}
	
	public UUID getFactionUUID() {
		return factionUUID;
	}
	
	public SerializableDateTime getSentTime() {
		return sentTime;
	}
	
	public UUID getInvitationUUID() {
		return invitationUUID;
	}
	
	public void setFromPlayerName(String s) {
		this.fromPlayerName = s;
	}
	
	public void setToPlayerName(String s) {
		this.toPlayerName = s;
	}
}
