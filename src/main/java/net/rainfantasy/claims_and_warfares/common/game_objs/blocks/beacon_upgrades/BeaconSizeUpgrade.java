package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BeaconSizeUpgrade extends BaseBeaconUpgradeBlock implements IBeaconUpgrade {
	
	@Override
	public void apply(BeaconUpgradeLoader loader, Level level, BlockPos pos) {
		loader.increaseSize(1);
		loader.increaseFuelCost(1);
	}
	
	@Override
	public String getId() {
		return "size_upgrade";
	}
}
