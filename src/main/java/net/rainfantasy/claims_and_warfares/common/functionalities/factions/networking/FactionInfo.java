package net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.INetworkInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FactionInfo implements INetworkInfo<FactionInfo> {
	
	UUID FactionUUID;
	String FactionName;
	Set<UUID> Members;
	Set<UUID> admins;
	UUID owner;
	int color;
	int fakePlayerPolicy;
	
	public FactionInfo(UUID factionUUID, String factionName, Set<UUID> members, Set<UUID> admins, UUID owner, int color, int fakePlayerPolicy) {
		FactionUUID = factionUUID;
		FactionName = factionName;
		Members = members;
		this.admins = admins;
		this.owner = owner;
		this.color = color;
		this.fakePlayerPolicy = fakePlayerPolicy;
	}
	
	public FactionInfo(FactionData factionData) {
		FactionUUID = factionData.getFactionUUID();
		FactionName = factionData.getFactionName();
		Members = factionData.getMembers();
		admins = factionData.getAdmins();
		owner = factionData.getOwner();
		color = factionData.getFactionColor();
		fakePlayerPolicy = factionData.getFakePlayerPolicy();
	}
	
	public FactionInfo() {
	}
	
	@Override
	public void toBytes(FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(FactionUUID);
		byteBuf.writeUtf(FactionName);
		byteBuf.writeCollection(new ArrayList<>(Members), FriendlyByteBuf::writeUUID);
		byteBuf.writeCollection(new ArrayList<>(admins), FriendlyByteBuf::writeUUID);
		byteBuf.writeUUID(owner);
		byteBuf.writeVarInt(color);
		byteBuf.writeVarInt(fakePlayerPolicy);
	}
	
	@Override
	public FactionInfo fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		String factionName = byteBuf.readUtf();
		Set<UUID> members = new HashSet<>(byteBuf.readList(FriendlyByteBuf::readUUID));
		Set<UUID> admins = new HashSet<>(byteBuf.readList(FriendlyByteBuf::readUUID));
		UUID owner = byteBuf.readUUID();
		int color = byteBuf.readVarInt();
		int fakePlayerPolicy = byteBuf.readVarInt();
		return new FactionInfo(factionUUID, factionName, members, admins, owner, color, fakePlayerPolicy);
	}
	
	public UUID getFactionUUID() {
		return FactionUUID;
	}
	
	public String getFactionName() {
		return FactionName;
	}
	
	public Set<UUID> getMembers() {
		return Members;
	}
	
	public Set<UUID> getAdmins() {
		return admins;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public int getColor() {
		return color;
	}
	
	public int getFakePlayerPolicy() {
		return fakePlayerPolicy;
	}
	
	public void trim() {
		this.admins.clear();
		this.Members.removeIf(uuid -> !uuid.equals(owner));
	}
}
