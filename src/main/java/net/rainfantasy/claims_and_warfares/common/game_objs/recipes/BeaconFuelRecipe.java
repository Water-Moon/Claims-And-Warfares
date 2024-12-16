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

public class BeaconFuelRecipe implements Recipe<SimpleContainer> {
	
	private final NonNullList<Ingredient> inputItems;
	private final Ingredient item;
	private final int fuelValue;
	private final ResourceLocation id;
	
	public BeaconFuelRecipe(Ingredient item, int fuelValue, ResourceLocation id) {
		this.item = item;
		this.inputItems = NonNullList.withSize(1, item);
		this.fuelValue = fuelValue;
		this.id = id;
	}
	
	@Override
	public boolean matches(@NotNull SimpleContainer pContainer, @NotNull Level pLevel) {
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
	
	public int getFuelValue() {
		return fuelValue;
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
	
	public static class Type implements RecipeType<BeaconFuelRecipe> {
		
		public static final Type INSTANCE = new Type();
		
		private Type() {
		}
	}
	
	public static class Serializer implements RecipeSerializer<BeaconFuelRecipe> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		private Serializer() {
		}
		
		@Override
		public @NotNull BeaconFuelRecipe fromJson(@NotNull ResourceLocation pId, @NotNull JsonObject pObject) {
			int amount = pObject.get("fuelValue").getAsInt();
			Ingredient item = Ingredient.fromJson(pObject.get("ingredient"));
			return new BeaconFuelRecipe(item, amount, pId);
		}
		
		@Override
		public @NotNull BeaconFuelRecipe fromNetwork(@NotNull ResourceLocation pId, @NotNull FriendlyByteBuf pBuffer) {
			int amount = pBuffer.readVarInt();
			Ingredient item = Ingredient.fromNetwork(pBuffer);
			return new BeaconFuelRecipe(item, amount, pId);
		}
		
		@Override
		public void toNetwork(@NotNull FriendlyByteBuf pBuffer, @NotNull BeaconFuelRecipe pRecipe) {
			pBuffer.writeVarInt(pRecipe.fuelValue);
			pRecipe.item.toNetwork(pBuffer);
		}
	}
}
