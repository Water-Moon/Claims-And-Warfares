package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;

import java.util.UUID;

/**
 * indicates that a claim is owned by a faction. <br>
 * Warning: claim with this feature *will* be *automatically removed* if the linked faction doesn't exist!
 */
public class FactionOwnedClaimFeature extends AbstractClaimFeature {
	
	final UUID factionUUID;
	int minAllowedDiplomaticLevelInteract = DiplomaticRelationshipData.ALLY;
	int minAllowedDiplomaticLevelBreakPlace = DiplomaticRelationshipData.ALLY;
	
	public static final int BREAK_PLACE = 0;
	public static final int INTERACT = 1;
	
	public FactionOwnedClaimFeature() {
		this(UUID.randomUUID());
	}
	
	public FactionOwnedClaimFeature(UUID factionUUID) {
		this.factionUUID = factionUUID;
	}
	
	@Override
	public String getName() {
		return "Faction Owned Claim";
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return CAWConstants.rl("faction_owned_claim");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		FactionOwnedClaimFeature feature = new FactionOwnedClaimFeature(UUID.fromString(nbt.getString("faction")));
		feature.minAllowedDiplomaticLevelInteract = nbt.getInt("minAllowedDiplomaticLevelInteract");
		feature.minAllowedDiplomaticLevelBreakPlace = nbt.getInt("minAllowedDiplomaticLevelBreakPlace");
		return feature;
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putString("faction", factionUUID.toString());
		nbt.putInt("minAllowedDiplomaticLevelInteract", minAllowedDiplomaticLevelInteract);
		nbt.putInt("minAllowedDiplomaticLevelBreakPlace", minAllowedDiplomaticLevelBreakPlace);
		return nbt;
	}
	
	public UUID getFactionUUID() {
		return factionUUID;
	}
	
	public boolean checkAllowed(Entity entity, int type) {
		//only protect against players
		
		if (entity == null) return true;
		if (!(entity instanceof ServerPlayer player)) return true;
		if (type == BREAK_PLACE) {
			return FactionClaimDataManager.isAuthorizedInFactionClaim(player, factionUUID, minAllowedDiplomaticLevelBreakPlace);
		} else if (type == INTERACT) {
			return FactionClaimDataManager.isAuthorizedInFactionClaim(player, factionUUID, minAllowedDiplomaticLevelInteract);
		} else {
			throw new IllegalArgumentException("Unknown interaction type " + type);
		}
	}
	
	public String getFactionName() {
		return FactionDataManager.get().getFactionName(factionUUID);
	}
	
	public void setInteractLevel(int interactDiplomaticLevel) {
		this.minAllowedDiplomaticLevelInteract = interactDiplomaticLevel;
	}
	
	public void setBreakPlaceLevel(int breakPlaceDiplomaticLevel) {
		this.minAllowedDiplomaticLevelBreakPlace = breakPlaceDiplomaticLevel;
	}
}
