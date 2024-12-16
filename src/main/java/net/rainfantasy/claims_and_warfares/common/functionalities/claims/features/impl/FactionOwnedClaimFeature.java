package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;

import java.util.Optional;
import java.util.UUID;

/**
 * indicates that a claim is owned by a faction. <br>
 * Warning: claim with this feature *will* be *automatically removed* if the linked faction doesn't exist!
 */
public class FactionOwnedClaimFeature extends AbstractClaimFeature {
	
	final UUID factionUUID;
	int minAllowedDiplomaticLevel = DiplomaticRelationshipData.ALLY;
	
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
		feature.minAllowedDiplomaticLevel = nbt.getInt("minAllowedDiplomaticLevel");
		return feature;
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putString("faction", factionUUID.toString());
		nbt.putInt("minAllowedDiplomaticLevel", minAllowedDiplomaticLevel);
		return nbt;
	}
	
	public UUID getFactionUUID() {
		return factionUUID;
	}
	
	public boolean checkAllowed(Entity entity) {
		//only protect against players
		
		if (entity == null) return true;
		if (!(entity instanceof ServerPlayer player)) return true;
		return FactionClaimDataManager.isAuthorizedInFactionClaim(player, factionUUID, minAllowedDiplomaticLevel);
	}
	
	public String getFactionName() {
		return FactionDataManager.get().getFactionName(factionUUID);
	}
	
}
