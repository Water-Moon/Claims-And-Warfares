package net.rainfantasy.claims_and_warfares.common.setups.data_types;

import net.minecraft.nbt.CompoundTag;

public class SerializableRegion2D implements ISerializableNBTData<SerializableRegion2D, CompoundTag> {
	
	int xmin;
	int zmin;
	int xmax;
	int zmax;
	
	public SerializableRegion2D(int xmin, int zmin, int xmax, int zmax) {
		this.xmin = xmin;
		this.zmin = zmin;
		this.xmax = xmax;
		this.zmax = zmax;
	}
	
	public boolean isIn(int x, int z) {
		return x >= xmin && x <= xmax && z >= zmin && z <= zmax;
	}
	
	public boolean isIn(float x, float z) {
		return x >= xmin && x <= xmax && z >= zmin && z <= zmax;
	}
	
	public int getXmin() {
		return xmin;
	}
	
	public int getZmin() {
		return zmin;
	}
	
	public int getXmax() {
		return xmax;
	}
	
	public int getZmax() {
		return zmax;
	}
	
	@Override
	public SerializableRegion2D readFromNBT(CompoundTag nbt) {
		int[] region = nbt.getIntArray("Region");
		return new SerializableRegion2D(region[0], region[1], region[2], region[3]);
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putIntArray("Region", new int[]{xmin, zmin, xmax, zmax});
		return nbt;
	}
}
