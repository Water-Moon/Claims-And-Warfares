package net.rainfantasy.claims_and_warfares.common.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconHackerFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeaconHackerFuelJEICategory implements IRecipeCategory<BeaconHackerFuelRecipe> {
	
	public static final ResourceLocation UID = CAWConstants.rl("beacon_hacker_fuel");
	public static final ResourceLocation TEXTURE = CAWConstants.rl("textures/gui/beacon_hack_jei.png");
	
	public static final RecipeType<BeaconHackerFuelRecipe> BEACON_HACKER_FUEL_TYPE = new RecipeType<>(UID, BeaconHackerFuelRecipe.class);
	
	private final IDrawable background;
	private final IDrawable icon;
	
	public BeaconHackerFuelJEICategory(IGuiHelper iGuiHelper) {
		this.background = iGuiHelper.createDrawable(TEXTURE, 0, 0, 176, 85);
		this.icon = iGuiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.BEACON_HACKER.get()));
	}
	
	
	@Override
	public @NotNull RecipeType<BeaconHackerFuelRecipe> getRecipeType() {
		return BEACON_HACKER_FUEL_TYPE;
	}
	
	@Override
	public @NotNull Component getTitle() {
		return Component.translatable("caw.gui.title.beacon_hacker");
	}
	
	@Override
	public @NotNull IDrawable getBackground() {
		return this.background;
	}
	
	@Override
	public @Nullable IDrawable getIcon() {
		return this.icon;
	}
	
	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BeaconHackerFuelRecipe recipe, @NotNull IFocusGroup focus) {
		builder.addSlot(RecipeIngredientRole.INPUT, 80, 52).addIngredients(recipe.getIngredients().get(0));
	}
	
	@Override
	public void draw(@NotNull BeaconHackerFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		
		
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		String timeString = String.valueOf(recipe.getFuelValue());
		int stringWidth = fontRenderer.width(timeString);
		int startX = 56 + (77 - stringWidth) / 2;
		guiGraphics.drawString(fontRenderer, timeString, startX, 15, ColorUtil.combine(255, 0, 0, 0), false);
	}
}
