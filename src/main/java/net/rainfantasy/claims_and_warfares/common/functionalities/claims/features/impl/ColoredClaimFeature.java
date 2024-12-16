package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;

public class ColoredClaimFeature extends AbstractClaimFeature {
	
	int color;
	
	public ColoredClaimFeature() {
		this.color = (255 << 24);
	}
	
	public ColoredClaimFeature(int color) {
		this.color = color;
	}
	
	@Override
	public String getName() {
		return "Colored Claim";
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return CAWConstants.rl("colored_claim");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		return new ColoredClaimFeature(nbt.getInt("color"));
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putInt("color", this.color);
		return nbt;
	}
	
	public int getColor() {
		return this.color;
	}
}
