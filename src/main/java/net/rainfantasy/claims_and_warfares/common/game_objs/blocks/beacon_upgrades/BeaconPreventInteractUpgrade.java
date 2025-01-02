package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

public class BeaconPreventInteractUpgrade extends AbstractBeaconUpgradeBlock {
	
	@Override
	public void apply(BeaconUpgradeLoader loader) {
		loader.setInteractProtection(true);
		loader.increaseFuelCost(1);
	}
	
	@Override
	public String getId() {
		return "explosion_protection";
	}
}
