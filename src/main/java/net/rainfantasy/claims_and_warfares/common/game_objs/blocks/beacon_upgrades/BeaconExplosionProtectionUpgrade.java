package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

public class BeaconExplosionProtectionUpgrade extends AbstractBeaconUpgradeBlock{
	
	@Override
	public void apply(BeaconUpgradeLoader loader) {
		loader.setExplosionProtection(true);
		loader.increaseFuelCost(8);
	}
	
	@Override
	public String getId() {
		return "explosion_protection";
	}
}
