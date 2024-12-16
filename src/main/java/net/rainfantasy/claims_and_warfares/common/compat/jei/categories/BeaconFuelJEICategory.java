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
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeaconFuelJEICategory implements IRecipeCategory<BeaconFuelRecipe> {
	
	public static final ResourceLocation UID = CAWConstants.rl("beacon_fuel");
	public static final ResourceLocation TEXTURE = CAWConstants.rl("textures/gui/beacon_jei.png");
	
	public static final RecipeType<BeaconFuelRecipe> BEACON_FUEL_TYPE = new RecipeType<>(UID, BeaconFuelRecipe.class);
	
	private final IDrawable background;
	private final IDrawable icon;
	
	public BeaconFuelJEICategory(IGuiHelper iGuiHelper) {
		this.background = iGuiHelper.createDrawable(TEXTURE, 0, 0, 176, 85);
		this.icon = iGuiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.CLAIM_BEACON.get()));
	}
	
	@Override
	public @NotNull RecipeType<BeaconFuelRecipe> getRecipeType() {
		return BEACON_FUEL_TYPE;
	}
	
	@Override
	public @NotNull Component getTitle() {
		return Component.translatable("caw.gui.title.claim_beacon");
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
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull BeaconFuelRecipe recipe, @NotNull IFocusGroup focus) {
		builder.addSlot(RecipeIngredientRole.INPUT, 80, 52).addIngredients(recipe.getIngredients().get(0));
	}
	
	@Override
	public void draw(@NotNull BeaconFuelRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		
		
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		int time = recipe.getFuelValue();
		int seconds = time % 60;
		int minutes = (time / 60) % 60;
		int hours = time / 3600;
		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		int stringWidth = fontRenderer.width(timeString);
		int startX = 56 + (77 - stringWidth) / 2;
		guiGraphics.drawString(fontRenderer, timeString, startX, 15, ColorUtil.combine(255, 0, 0, 0), false);
	}
}
