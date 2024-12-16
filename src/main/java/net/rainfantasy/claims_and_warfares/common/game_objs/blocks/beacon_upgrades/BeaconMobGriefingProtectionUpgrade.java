package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

public class BeaconMobGriefingProtectionUpgrade extends AbstractBeaconUpgradeBlock {
	
	@Override
	public void apply(BeaconUpgradeLoader loader) {
		loader.setMobGriefProtection(true);
		loader.increaseFuelCost(2);
	}
	
	@Override
	public String getId() {
		return "explosion_protection";
	}
}
