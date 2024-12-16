package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionData implements ISerializableNBTData<FactionData, CompoundTag> {
	
	private final UUID FactionUUID;
	boolean canHaveClaims = true;
	String factionName = "";
	int factionColor = ColorUtil.randomMinecraftColor();
	HashMap<UUID, DiplomaticRelationshipData> diplomaticRelationships = new HashMap<>();
	int fakePlayerPolicy = 0;
	HashMap<UUID, FactionMemberData> members = new HashMap<>();
	
	public static final int FAKE_PLAYER_POLICY_CHECK_UUID = 0;
	public static final int FAKE_PLAYER_POLICY_ALLOW = 1;
	public static final int FAKE_PLAYER_POLICY_DENY = -1;
	
	
	public FactionData() {
		this.FactionUUID = UUID.randomUUID();
	}
	
	private FactionData(UUID FactionUUID) {
		this.FactionUUID = FactionUUID;
	}
	
	FactionData(UUID FactionUUID, String factionName, ServerPlayer creator) {
		this.FactionUUID = FactionUUID;
		this.factionName = factionName;
		this.members.put(creator.getUUID(), new FactionMemberData(creator.getUUID(), creator.getName().getString(), true, true));
	}
	
	////////
	
	
	public UUID getFactionUUID() {
		return FactionUUID;
	}
	
	public boolean is(UUID otherFactionUUID) {
		return this.getFactionUUID().equals(otherFactionUUID);
	}
	
	public String getFactionName() {
		return factionName;
	}
	
	private void refresh(MinecraftServer server) {
		this.deduplicateMembers();
		this.refreshPlayerKnownNames(server);
		FactionDataManager.get().refresh();
	}
	
	public void refreshData(MinecraftServer server) {
		this.deduplicateMembers();
		this.refreshPlayerKnownNames(server);
	}
	
	private void refreshPlayerKnownNames(MinecraftServer server) {
		PlayerList playerList = server.getPlayerList();
		for (Entry<UUID, FactionMemberData> entry : this.members.entrySet()) {
			ServerPlayer player = playerList.getPlayer(entry.getKey());
			if (player != null) {
				entry.getValue().knownPlayerName = player.getName().getString();
			}
		}
	}
	
	private void deduplicateMembers() {
		Set<UUID> duplicates = this.members.entrySet().stream().filter(e -> this.members.values().stream().filter(m -> m.playerUUID.equals(e.getValue().playerUUID)).count() > 1).map(Entry::getKey).collect(Collectors.toSet());
		duplicates.forEach(this.members::remove);
	}
	
	/**
	 * Returns the UUID of the owner of the faction
	 *
	 * @return The UUID of the owner of the faction
	 * @throws IllegalStateException if no owner is found (a faction *must* have an owner)
	 */
	public UUID getOwnerUUID() {
		return this.members.entrySet().stream().filter(e -> e.getValue().isOwner).map(Entry::getKey).findFirst().orElseThrow(
		() -> new IllegalStateException("No owner found for faction " + this.FactionUUID));
	}
	
	/**
	 * Checks if the player with the given UUID is an admin or the owner of the faction.
	 *
	 * @param playerUUID The UUID of the player to check.
	 * @return true if the player is an admin or the owner, false otherwise.
	 */
	public boolean isAdminOrOwner(UUID playerUUID) {
		if(!this.members.containsKey(playerUUID)) return false;
		return this.members.get(playerUUID).isAdmin || this.members.get(playerUUID).isOwner;
	}
	
	public boolean isMember(UUID playerUUID) {
		return this.members.containsKey(playerUUID);
	}
	
	/**
	 * Adds a new member to the faction.
	 *
	 * @param playerUUID The UUID of the player to add.
	 * @param name       The name of the player to add.
	 * @return true if the member was added successfully, false if the player is already a member.
	 */
	public boolean addMember(UUID playerUUID, String name) {
		if (this.members.containsKey(playerUUID)) return false;
		this.members.put(playerUUID, new FactionMemberData(playerUUID, name));
		return true;
	}
	
	/**
	 * Adds a new member to the faction using a ServerPlayer object.
	 *
	 * @param player The ServerPlayer object representing the player to add.
	 * @return true if the member was added successfully, false if the player is already a member.
	 */
	public boolean addMember(ServerPlayer player) {
		return this.addMember(player.getUUID(), player.getName().getString());
	}
	
	/**
	 * Removes a member from the faction.
	 *
	 * @param playerUUID The UUID of the player to remove.
	 * @return true if the member was removed successfully, false if the player was not a member.
	 */
	public boolean removeMember(UUID playerUUID) {
		if (!this.members.containsKey(playerUUID)) return false;
		this.members.remove(playerUUID);
		return true;
	}
	
	/**
	 * Removes a member from the faction using a ServerPlayer object.
	 *
	 * @param player The ServerPlayer object representing the player to remove.
	 * @return true if the member was removed successfully, false if the player was not a member.
	 */
	public boolean removeMember(ServerPlayer player) {
		return this.removeMember(player.getUUID());
	}
	
	public boolean canHaveClaims() {
		return this.canHaveClaims;
	}
	
	public void setCanHaveClaims(boolean canHaveClaims) {
		this.canHaveClaims = canHaveClaims;
	}
	
	public String getName() {
		return this.getFactionName();
	}
	
	public void setName(String name) {
		this.factionName = name;
	}
	
	public int getFactionColor() {
		return factionColor;
	}
	
	public void setFactionColor(int factionColor) {
		this.factionColor = factionColor;
	}
	
	@Override
	public FactionData readFromNBT(CompoundTag nbt) {
		FactionData result = new FactionData(UUID.fromString(nbt.getString("FactionUUID")));
		result.canHaveClaims = nbt.getBoolean("canHaveClaims");
		result.factionName = nbt.getString("factionName");
		result.factionColor = nbt.getInt("color");
		result.fakePlayerPolicy = nbt.getInt("fakePlayerPolicy");
		
		ListTag diplomaticRelationshipsTag = nbt.getList("diplomaticRelationships", ListTag.TAG_COMPOUND);
		diplomaticRelationshipsTag.forEach(tag -> {
			CompoundTag relationshipTag = (CompoundTag) tag;
			UUID factionUUID = UUID.fromString(relationshipTag.getString("FactionUUID"));
			DiplomaticRelationshipData relationship = new DiplomaticRelationshipData(null).readFromNBT(relationshipTag);
			result.diplomaticRelationships.put(factionUUID, relationship);
		});
		
		ListTag membersTag = nbt.getList("members", ListTag.TAG_COMPOUND);
		membersTag.forEach(tag -> {
			CompoundTag memberTag = (CompoundTag) tag;
			UUID playerUUID = UUID.fromString(memberTag.getString("playerUUID"));
			FactionMemberData member = new FactionMemberData().readFromNBT(memberTag);
			result.members.put(playerUUID, member);
		});
		
		return result;
	}
	
	////////
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("FactionUUID", this.FactionUUID.toString());
		nbt.putBoolean("canHaveClaims", this.canHaveClaims);
		nbt.putString("factionName", this.factionName);
		nbt.putInt("color", this.factionColor);
		nbt.putInt("fakePlayerPolicy", this.fakePlayerPolicy);
		
		ListTag diplomaticRelationshipsTag = new ListTag();
		this.diplomaticRelationships.forEach((uuid, relationship) -> {
			CompoundTag relationshipTag = relationship.writeToNBT(new CompoundTag());
			relationshipTag.putString("FactionUUID", uuid.toString());
			diplomaticRelationshipsTag.add(relationshipTag);
		});
		nbt.put("diplomaticRelationships", diplomaticRelationshipsTag);
		
		ListTag membersTag = new ListTag();
		this.members.forEach((uuid, member) -> {
			CompoundTag memberTag = member.writeToNBT(new CompoundTag());
			memberTag.putString("playerUUID", uuid.toString());
			membersTag.add(memberTag);
		});
		nbt.put("members", membersTag);
		
		return nbt;
	}
	
	@Override
	public String toString() {
		return "FactionData{" +
		       "FactionUUID=" + FactionUUID +
		       ", canHaveClaims=" + canHaveClaims +
		       ", factionName='" + factionName + '\'' +
		       ", diplomaticRelationships=" + diplomaticRelationships +
		       ", members=" + members +
		       '}';
	}
	
	public boolean canRemoveMember(UUID playerUUID) {
		if(!this.members.containsKey(playerUUID)) return false;
		//false if owner or admin
		return !this.members.get(playerUUID).isOwner && !this.members.get(playerUUID).isAdmin;
	}
	
	public Set<UUID> getMembers() {
		return this.members.keySet();
	}
	
	public Set<UUID> getAdmins() {
		return this.members.entrySet().stream().filter(e -> e.getValue().isAdmin).map(Entry::getKey).collect(Collectors.toSet());
	}
	
	public FactionMemberData getDataOf(UUID playerUUID) {
		return this.members.get(playerUUID);
	}
	
	public UUID getOwner() {
		return this.members.entrySet().stream().filter(e -> e.getValue().isOwner).map(Entry::getKey).findFirst().orElseThrow(
		() -> new IllegalStateException("No owner found for faction " + this.FactionUUID)
		);
	}
	
	/**
	 * Sets the owner of the faction to the player with the given UUID
	 * All other members will have their isOwner field set to false
	 *
	 * @param playerUUID The UUID of the player to set as owner
	 */
	public void setOwner(UUID playerUUID) {
		this.members.values().forEach(m -> {
			m.isOwner = m.playerUUID.equals(playerUUID);
			if (m.isOwner) {
				m.isAdmin = true;
			}
		});
	}
	
	public boolean isAdmin(UUID targetPlayerUUID) {
		if(!this.members.containsKey(targetPlayerUUID)) return false;
		return this.members.get(targetPlayerUUID).isAdmin;
	}
	
	public void addAdmin(UUID targetPlayerUUID) {
		if(!this.members.containsKey(targetPlayerUUID)) return;
		this.members.get(targetPlayerUUID).isAdmin = true;
	}
	
	public void removeAdmin(UUID targetPlayerUUID) {
		if(!this.members.containsKey(targetPlayerUUID)) return;
		this.members.get(targetPlayerUUID).isAdmin = false;
	}
	
	public boolean isOwner(UUID targetPlayerUUID) {
		if(!this.members.containsKey(targetPlayerUUID)) return false;
		return this.members.get(targetPlayerUUID).isOwner;
	}
	
	public HashMap<UUID, DiplomaticRelationshipData> getDiplomaticRelationships() {
		return diplomaticRelationships;
	}
	
	public void setDiplomaticRelationship(UUID otherFaction, int relationship) {
		this.diplomaticRelationships.put(otherFaction, new DiplomaticRelationshipData(otherFaction, relationship));
	}
	
	public int getDiplomaticRelationship(UUID otherFaction) {
		return this.diplomaticRelationships.getOrDefault(otherFaction, new DiplomaticRelationshipData(otherFaction)).relationship;
	}
	
	public int getFakePlayerPolicy() {
		return fakePlayerPolicy;
	}
	
	public void setFakePlayerPolicy(int policy) {
		this.fakePlayerPolicy = policy;
	}
}
