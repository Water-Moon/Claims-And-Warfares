package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.items.ClaimViewerItem;
import net.rainfantasy.claims_and_warfares.common.game_objs.items.TeamManageItem;

@SuppressWarnings("unused")
public class ItemRegistry {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CAWConstants.MODID);
	
	public static RegistryObject<Item> CLAIM_VIEWER = ITEMS.register("claim_viewer", ClaimViewerItem::new);
	public static RegistryObject<Item> TEAM_MANAGER = ITEMS.register("team_manager", TeamManageItem::new);
	
	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
	
}
