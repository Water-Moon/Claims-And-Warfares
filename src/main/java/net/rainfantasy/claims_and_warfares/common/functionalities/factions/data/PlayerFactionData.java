package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class PlayerFactionData implements ISerializableNBTData<PlayerFactionData, CompoundTag> {
	
	UUID playerUUID;
	Set<UUID> factions = new CopyOnWriteArraySet<>();
	private UUID primaryFaction = null;
	
	public PlayerFactionData() {
		this.playerUUID = UUID.randomUUID();
	}
	
	public PlayerFactionData(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}
	
	private PlayerFactionData(UUID playerUUID, Set<UUID> factions, UUID primaryFaction) {
		this.playerUUID = playerUUID;
		this.factions = factions;
		this.primaryFaction = primaryFaction;
	}
	
	public void setPrimaryFaction(UUID primaryFaction) {
		this.primaryFaction = primaryFaction;
	}
	
	public Optional<UUID> getPrimaryFactionUUID() {
		return Optional.ofNullable(primaryFaction);
	}
	
	public ImmutableSet<UUID> getFactions() {
		return ImmutableSet.copyOf(factions);
	}
	
	public void removeFaction(UUID factionUUID) {
		factions.remove(factionUUID);
		if (this.primaryFaction != null && this.primaryFaction.equals(factionUUID)) {
			this.primaryFaction = null;
		}
	}
	
	@Override
	public PlayerFactionData readFromNBT(CompoundTag nbt) {
		this.playerUUID = UUID.fromString(nbt.getString("playerUUID"));
		this.factions = nbt.getList("factions", ListTag.TAG_STRING).stream().map(tag -> UUID.fromString(tag.getAsString())).collect(Collectors.toSet());
		String primaryFactionString = nbt.getString("primaryFaction");
		this.primaryFaction = primaryFactionString.isEmpty() ? null : UUID.fromString(primaryFactionString);
		return this;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("playerUUID", playerUUID.toString());
		ListTag factionsTag = new ListTag();
		for (UUID factionUUID : factions) {
			factionsTag.add(StringTag.valueOf(factionUUID.toString()));
		}
		nbt.put("factions", factionsTag);
		nbt.putString("primaryFaction", primaryFaction == null ? "" : primaryFaction.toString());
		return nbt;
	}
}
