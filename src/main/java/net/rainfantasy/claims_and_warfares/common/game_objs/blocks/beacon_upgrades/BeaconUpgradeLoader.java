package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;

import java.util.HashMap;

import static net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData.ALLY;

public class BeaconUpgradeLoader implements ISerializableNBTData<BeaconUpgradeLoader, CompoundTag> {
	
	private static final int MAX_SIZE_INCREASE = 3;
	
	HashMap<String, Integer> totalUpgradeCount = new HashMap<>();
	int sizeIncrease = 0;
	int increaseFuelCost = 0;
	boolean interactProtection = false;
	boolean mobGriefProtection = false;
	boolean explosionProtection = false;
	int interactDiplomaticLevel = ALLY;
	int breakPlaceDiplomaticLevel = ALLY;
	
	public BeaconUpgradeLoader() {
	
	}
	
	public int getTotalUpgradeCount() {
		return this.totalUpgradeCount.values().stream().mapToInt(Integer::intValue).sum();
	}
	
	public void doApply(IBeaconUpgrade upgrade, Level level, BlockPos pos) {
		String upgradeName = upgrade.getId();
		if (this.totalUpgradeCount.containsKey(upgradeName)) {
			this.totalUpgradeCount.put(upgradeName, this.totalUpgradeCount.get(upgradeName) + 1);
		} else {
			this.totalUpgradeCount.put(upgradeName, 1);
		}
		upgrade.apply(this, level, pos);
	}
	
	public void increaseSize(int size) {
		this.sizeIncrease += size;
		if (this.sizeIncrease > MAX_SIZE_INCREASE) this.sizeIncrease = MAX_SIZE_INCREASE;
	}
	
	public void increaseFuelCost(int fuelCost) {
		this.increaseFuelCost += fuelCost;
	}
	
	public void setInteractProtection(boolean interactProtection) {
		this.interactProtection = interactProtection;
	}
	
	public void setMobGriefProtection(boolean mobGriefProtection) {
		this.mobGriefProtection = mobGriefProtection;
	}
	
	public void setExplosionProtection(boolean explosionProtection) {
		this.explosionProtection = explosionProtection;
	}
	
	@Override
	public BeaconUpgradeLoader readFromNBT(CompoundTag nbt) {
		this.totalUpgradeCount.clear();
		ListTag upgradeList = nbt.getList("upgrades", 10);
		for (int i = 0; i < upgradeList.size(); i++) {
			CompoundTag upgradeTag = upgradeList.getCompound(i);
			String upgradeName = upgradeTag.getString("name");
			int count = upgradeTag.getInt("count");
			this.totalUpgradeCount.put(upgradeName, count);
		}
		this.sizeIncrease = nbt.getInt("sizeIncrease");
		this.increaseFuelCost = nbt.getInt("increaseFuelCost");
		this.mobGriefProtection = nbt.getBoolean("mobGriefProtection");
		this.explosionProtection = nbt.getBoolean("explosionProtection");
		this.interactProtection = nbt.getBoolean("interactProtection");
		
		this.interactDiplomaticLevel = nbt.getInt("interactTrust");
		this.breakPlaceDiplomaticLevel = nbt.getInt("breakPlaceTrust");
		
		return this;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		ListTag upgradeList = new ListTag();
		this.totalUpgradeCount.forEach((upgradeName, count) -> {
			CompoundTag upgradeTag = new CompoundTag();
			upgradeTag.putString("name", upgradeName);
			upgradeTag.putInt("count", count);
			upgradeList.add(upgradeTag);
		});
		nbt.put("upgrades", upgradeList);
		nbt.putInt("sizeIncrease", this.sizeIncrease);
		nbt.putInt("increaseFuelCost", this.increaseFuelCost);
		nbt.putBoolean("mobGriefProtection", this.mobGriefProtection);
		nbt.putBoolean("explosionProtection", this.explosionProtection);
		nbt.putBoolean("interactProtection", this.interactProtection);
		
		nbt.putInt("interactTrust", interactDiplomaticLevel);
		nbt.putInt("breakPlaceTrust", breakPlaceDiplomaticLevel);
		
		return nbt;
	}
	
	public boolean hasSameUpgrade(BeaconUpgradeLoader other) {
		if (this.totalUpgradeCount.size() != other.totalUpgradeCount.size()) return false;
		for (String upgradeName : this.totalUpgradeCount.keySet()) {
			if (!other.totalUpgradeCount.containsKey(upgradeName)) return false;
			if (!this.totalUpgradeCount.get(upgradeName).equals(other.totalUpgradeCount.get(upgradeName))) return false;
		}
		return true;
	}
	
	public int getSizeIncrease() {
		return sizeIncrease;
	}
	
	public int getIncreaseFuelCost() {
		return increaseFuelCost;
	}
	
	public boolean isInteractProtection() {
		return interactProtection;
	}
	
	public boolean isMobGriefProtection() {
		return mobGriefProtection;
	}
	
	public boolean isExplosionProtection() {
		return explosionProtection;
	}
	
	public int getInteractDiplomaticLevel() {
		return interactDiplomaticLevel;
	}
	
	public int getBreakPlaceDiplomaticLevel() {
		return breakPlaceDiplomaticLevel;
	}
	
	public void setInteractDiplomaticLevel(int interactDiplomaticLevel) {
		this.interactDiplomaticLevel = interactDiplomaticLevel;
	}
	
	public void setBreakPlaceDiplomaticLevel(int breakPlaceDiplomaticLevel) {
		this.breakPlaceDiplomaticLevel = breakPlaceDiplomaticLevel;
	}
}
