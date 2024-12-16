package net.rainfantasy.claims_and_warfares.common.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.gui.BeaconHackerScreen;
import net.rainfantasy.claims_and_warfares.client.gui.ClaimBeaconScreen;
import net.rainfantasy.claims_and_warfares.common.compat.jei.categories.BeaconFuelJEICategory;
import net.rainfantasy.claims_and_warfares.common.compat.jei.categories.BeaconHackerFuelJEICategory;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconFuelRecipe.Type;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconHackerFuelRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class CAWJeiPlugin implements IModPlugin {
	
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return CAWConstants.rl("jei_plugin");
	}
	
	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new BeaconFuelJEICategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new BeaconHackerFuelJEICategory(registration.getJeiHelpers().getGuiHelper()));
	}
	
	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		if(Minecraft.getInstance().level == null) {
			return;
		}
		
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		
		List<BeaconFuelRecipe> fuelRecipes = recipeManager.getAllRecipesFor(Type.INSTANCE);
		registration.addRecipes(BeaconFuelJEICategory.BEACON_FUEL_TYPE, fuelRecipes);
		List<BeaconHackerFuelRecipe> hackerFuelRecipes = recipeManager.getAllRecipesFor(BeaconHackerFuelRecipe.Type.INSTANCE);
		registration.addRecipes(BeaconHackerFuelJEICategory.BEACON_HACKER_FUEL_TYPE, hackerFuelRecipes);
	}
	
	@Override
	public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(ClaimBeaconScreen.class, 132, 84, 20, 20, BeaconFuelJEICategory.BEACON_FUEL_TYPE);
		registration.addRecipeClickArea(BeaconHackerScreen.class, 7, 99, 20, 17, BeaconHackerFuelJEICategory.BEACON_HACKER_FUEL_TYPE);
	}
}
