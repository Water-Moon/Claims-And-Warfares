package net.rainfantasy.claims_and_warfares.datagen;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BlockDrops extends BlockLootSubProvider {
	
	public BlockDrops() {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags());
	}
	
	@Override
	protected void generate() {
		this.dropSelf(BlockRegistry.CLAIM_BEACON.get());
		this.dropSelf(BlockRegistry.BEACON_HACKER.get());
		this.dropSelf(BlockRegistry.BEACON_UPGRADE_SIZE.get());
		this.dropSelf(BlockRegistry.BEACON_UPGRADE_MOB_GRIEFING.get());
		this.dropSelf(BlockRegistry.BEACON_UPGRADE_EXPLOSION_PROTECTION.get());
	}
	
	@Override
	protected @NotNull Iterable<Block> getKnownBlocks() {
		return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
	}
}
