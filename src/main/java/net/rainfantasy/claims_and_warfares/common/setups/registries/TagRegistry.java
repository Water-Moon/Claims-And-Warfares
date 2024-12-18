package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.rainfantasy.claims_and_warfares.CAWConstants;

public class TagRegistry {
	public static class Blocks{
		public static final TagKey<Block> ALLOW_INTERACT = TagKey.create(Registries.BLOCK, CAWConstants.rl("beacon_allow_interact"));
		public static final TagKey<Block> ALLOW_BREAK = TagKey.create(Registries.BLOCK, CAWConstants.rl("beacon_allow_break"));
		public static final TagKey<Block> ALLOW_PLACE = TagKey.create(Registries.BLOCK, CAWConstants.rl("beacon_allow_place"));
	}
}
