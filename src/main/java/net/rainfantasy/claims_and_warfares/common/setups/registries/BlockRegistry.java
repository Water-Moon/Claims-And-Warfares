package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.BeaconHackerBlock;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.ClaimBeaconBlock;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BeaconExplosionProtectionUpgrade;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BeaconMobGriefingProtectionUpgrade;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BeaconSizeUpgrade;

import java.util.function.Supplier;

@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
public class BlockRegistry {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CAWConstants.MODID);
	
	public static RegistryObject<Block> CLAIM_BEACON = registerBlock("claim_beacon", ClaimBeaconBlock::new);
	public static RegistryObject<Block> BEACON_HACKER = registerBlock("beacon_hacker", BeaconHackerBlock::new);
	
	public static RegistryObject<Block> BEACON_UPGRADE_SIZE = registerBlock("beacon_upgrade_size", BeaconSizeUpgrade::new);
	public static RegistryObject<Block> BEACON_UPGRADE_MOB_GRIEFING = registerBlock("beacon_upgrade_mob_grief", BeaconMobGriefingProtectionUpgrade::new);
	public static RegistryObject<Block> BEACON_UPGRADE_EXPLOSION_PROTECTION = registerBlock("beacon_upgrade_explosion_protection", BeaconExplosionProtectionUpgrade::new);
	
	private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
		RegistryObject<T> result = BLOCKS.register(name, block);
		registerBlockItem(name, result);
		return result;
	}
	
	private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
		return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Properties()));
	}
	
	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
}
