package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.BeaconLinkedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.DebugClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.DebugFullyProtectedClaim;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;

import java.util.HashMap;
import java.util.function.Function;

public class ClaimFeatureLoader {
	
	static HashMap<ResourceLocation, Function<CompoundTag, AbstractClaimFeature>> loaders = new HashMap<>();
	
	static {
		register(new DebugClaimFeature());
		register(new DebugFullyProtectedClaim());
		register(new FactionOwnedClaimFeature());
		register(new BeaconLinkedClaimFeature());
	}
	
	public static AbstractClaimFeature read(CompoundTag nbt) {
		ResourceLocation type = new ResourceLocation(nbt.getString("type"));
		CompoundTag data = nbt.getCompound("data");
		if (loaders.containsKey(type)) {
			AbstractClaimFeature result = loaders.get(type).apply(data);
			if (result == null) {
				throw new NullPointerException("Failed to load claim feature: " + type);
			}
			return result;
		}
		CAWConstants.LOGGER.error("Unknown claim feature type: {}", type);
		CAWConstants.LOGGER.error("Data: {}", data);
		CAWConstants.LOGGER.error("All loaders: {}", loaders.keySet());
		throw new UnsupportedOperationException("Unknown claim feature type: " + type);
	}
	
	public static CompoundTag write(AbstractClaimFeature feature, CompoundTag nbt) {
		nbt.putString("type", feature.getRegistryName().toString());
		CompoundTag data = feature.writeToNBT(new CompoundTag());
		nbt.put("data", data);
		return nbt;
	}
	
	public static void register(AbstractClaimFeature instance) {
		try {
			loaders.put(instance.getRegistryName(), instance::readFromNBT);
		} catch (Exception e) {
			CAWConstants.LOGGER.error("Failed to register claim feature: {}", instance.getName());
			CAWConstants.LOGGER.error("Error: ", e);
		}
	}
}
