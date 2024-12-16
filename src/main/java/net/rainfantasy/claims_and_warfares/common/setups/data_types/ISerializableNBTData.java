package net.rainfantasy.claims_and_warfares.common.setups.data_types;

import net.minecraft.nbt.Tag;

public interface ISerializableNBTData<T, P extends Tag> {
	
	T readFromNBT(P nbt);
	
	P writeToNBT(P nbt);
}
