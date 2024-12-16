package net.rainfantasy.claims_and_warfares.common.utils;


import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.mixin.accessors.WorldGenRegionAccessor;

public class MinecraftUtils {
	
	public static String getLevelId(Level level) {
		return level.dimension().location().toString();
	}
	
	public static String getLevelId(LevelAccessor level) {
		if (level instanceof Level l) {
			return getLevelId(l);
		} else if (level instanceof WorldGenRegion w) {
			return getLevelId(((WorldGenRegionAccessor) w).getLevel());
		} else {
			CAWConstants.LOGGER.warn("Unknown level accessor type: {}", level.getClass().getName());
			return "unknown";
		}
	}
}
