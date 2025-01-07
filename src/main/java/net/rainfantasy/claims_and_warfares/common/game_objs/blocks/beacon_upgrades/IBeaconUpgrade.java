package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IBeaconUpgrade {
	
	void apply(BeaconUpgradeLoader loader, Level level, BlockPos pos);
	
	String getId();
}
