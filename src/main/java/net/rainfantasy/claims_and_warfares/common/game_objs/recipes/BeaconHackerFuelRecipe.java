package net.rainfantasy.claims_and_warfares.common.game_objs.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeaconHackerFuelRecipe implements Recipe<SimpleContainer> {
	
	private final NonNullList<Ingredient> inputItems;
	private final Ingredient item;
	private final int fuelValue;
	private final ResourceLocation id;
	
	public BeaconHackerFuelRecipe(Ingredient item, int fuelValue, ResourceLocation id) {
		this.item = item;
		this.inputItems = NonNullList.withSize(1, item);
		this.fuelValue = fuelValue;
		this.id = id;
	}
	
	@Override
	public boolean matches(@NotNull SimpleContainer pContainer, Level pLevel) {
		if (pLevel.isClientSide()) {
			return false;
		}
		
		return item.test(pContainer.getItem(0));
	}
	
	@Override
	public @NotNull ItemStack assemble(@NotNull SimpleContainer pContainer, @NotNull RegistryAccess pRegistryAccess) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return true;
	}
	
	@Override
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public @NotNull ResourceLocation getId() {
		return id;
	}
	
	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	@Override
	public @NotNull RecipeType<?> getType() {
		return Type.INSTANCE;
	}
	
	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return inputItems;
	}
	
	public int getFuelValue() {
		return fuelValue;
	}
	
	public static class Type implements RecipeType<BeaconHackerFuelRecipe> {
		
		public static final Type INSTANCE = new Type();
		public static final String ID = "beacon_hacker_fuel";
		
		private Type() {
		}
	}
	
	public static class Serializer implements RecipeSerializer<BeaconHackerFuelRecipe> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public @NotNull BeaconHackerFuelRecipe fromJson(@NotNull ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
			int fuelValue = pSerializedRecipe.get("fuelValue").getAsInt();
			Ingredient item = Ingredient.fromJson(pSerializedRecipe.get("ingredient"));
			return new BeaconHackerFuelRecipe(item, fuelValue, pRecipeId);
		}
		
		@Override
		public @Nullable BeaconHackerFuelRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			int fuelValue = pBuffer.readVarInt();
			Ingredient item = Ingredient.fromNetwork(pBuffer);
			return new BeaconHackerFuelRecipe(item, fuelValue, pRecipeId);
		}
		
		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, BeaconHackerFuelRecipe pRecipe) {
			pBuffer.writeVarInt(pRecipe.fuelValue);
			pRecipe.item.toNetwork(pBuffer);
		}
	}
}
