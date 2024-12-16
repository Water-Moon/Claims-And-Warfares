package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;

import java.util.UUID;

public class DiplomaticRelationshipData implements ISerializableNBTData<DiplomaticRelationshipData, CompoundTag> {
	
	public static int NEUTRAL = 0;
	public static int ALLY = 1;
	public static int ENEMY = -1;
	
	UUID otherFaction;
	int relationship;
	
	public DiplomaticRelationshipData(UUID otherFaction) {
		this(otherFaction, 0);
	}
	
	public DiplomaticRelationshipData(UUID otherFaction, int relationship) {
		this.otherFaction = otherFaction;
		this.relationship = relationship;
	}
	
	@Override
	public DiplomaticRelationshipData readFromNBT(CompoundTag nbt) {
		this.otherFaction = UUID.fromString(nbt.getString("otherFaction"));
		this.relationship = nbt.getInt("relationship");
		return this;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("otherFaction", otherFaction.toString());
		nbt.putInt("relationship", relationship);
		return nbt;
	}
	
	public UUID getOtherFaction() {
		return otherFaction;
	}
	
	public int getRelationship() {
		return relationship;
	}
}