package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.BeaconHackerMenu;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;

public class MenuRegistry {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CAWConstants.MODID);
	
	public static final RegistryObject<MenuType<ClaimBeaconMenu>> CLAIM_BEACON_MENU =
	MENUS.register("claim_beacon_menu", () -> IForgeMenuType.create(ClaimBeaconMenu::new));
	public static final RegistryObject<MenuType<BeaconHackerMenu>> BEACON_HACKER_MENU =
	MENUS.register("beacon_hacker_menu", () -> IForgeMenuType.create(BeaconHackerMenu::new));
	
	public static void register(IEventBus eventBus) {
		MENUS.register(eventBus);
	}
}
