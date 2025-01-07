package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BeaconExplosionProtectionUpgrade extends BaseBeaconUpgradeBlock implements IBeaconUpgrade {
	
	@Override
	public void apply(BeaconUpgradeLoader loader, Level level, BlockPos pos) {
		loader.setExplosionProtection(true);
		loader.increaseFuelCost(8);
	}
	
	@Override
	public String getId() {
		return "explosion_protection";
	}
}
