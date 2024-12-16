package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;

@SuppressWarnings("DataFlowIssue")
public class BlockEntityRegistry {
	
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CAWConstants.MODID);
	
	public static RegistryObject<BlockEntityType<ClaimBeaconBlockEntity>> CLAIM_BEACON_BE = BLOCK_ENTITIES.register("claim_beacon_be", () ->
	                                                                                                                                   BlockEntityType.Builder.of(ClaimBeaconBlockEntity::new, BlockRegistry.CLAIM_BEACON.get()).build(null)
	);
	public static RegistryObject<BlockEntityType<BeaconHackerBlockEntity>> BEACON_HACKER_BE = BLOCK_ENTITIES.register("beacon_hacker_be", () ->
	                                                                                                                                      BlockEntityType.Builder.of(BeaconHackerBlockEntity::new, BlockRegistry.BEACON_HACKER.get()).build(null)
	);
	
	public static void register(IEventBus eventBus) {
		BLOCK_ENTITIES.register(eventBus);
	}
}
