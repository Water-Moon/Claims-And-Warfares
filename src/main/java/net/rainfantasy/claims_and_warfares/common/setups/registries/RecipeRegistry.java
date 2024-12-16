package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconHackerFuelRecipe;

public class RecipeRegistry {
	
	public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CAWConstants.MODID);
	
	public static final RegistryObject<RecipeSerializer<BeaconFuelRecipe>> BEACON_FUEL = RECIPES.register("beacon_fuel", () -> BeaconFuelRecipe.Serializer.INSTANCE);
	public static final RegistryObject<RecipeSerializer<BeaconHackerFuelRecipe>> BEACON_HACKER_FUEL = RECIPES.register("beacon_hacker_fuel", () -> BeaconHackerFuelRecipe.Serializer.INSTANCE);
	
	public static void register(IEventBus eventBus) {
		RECIPES.register(eventBus);
	}
}
