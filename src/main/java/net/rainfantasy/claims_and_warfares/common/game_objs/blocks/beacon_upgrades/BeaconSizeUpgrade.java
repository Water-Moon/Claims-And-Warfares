package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

public class BeaconSizeUpgrade extends AbstractBeaconUpgradeBlock {
	
	@Override
	public void apply(BeaconUpgradeLoader loader) {
		loader.increaseSize(1);
		loader.increaseFuelCost(1);
	}
	
	@Override
	public String getId() {
		return "size_upgrade";
	}
}
