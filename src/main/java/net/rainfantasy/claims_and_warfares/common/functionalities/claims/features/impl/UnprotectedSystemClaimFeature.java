package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;

import java.util.UUID;

public class UnprotectedSystemClaimFeature extends AbstractClaimFeature {
	
	UUID claimUUID;
	
	public UnprotectedSystemClaimFeature() {
		this(UUID.randomUUID());
	}
	
	public UnprotectedSystemClaimFeature(UUID claimUUID) {
		this.claimUUID = claimUUID;
	}
	
	@Override
	public String getName() {
		return "System Claim (Unprotected)";
	}
	
	@Override
	public Component getDisplayName() {
		return Component.translatable("caw.gui.label.claim_system_unprotected");
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return new ResourceLocation("caw", "system_claim_unprotected");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		UUID claimUUID = UUID.fromString(nbt.getString("uuid"));
		return new UnprotectedSystemClaimFeature(claimUUID);
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putString("uuid", claimUUID.toString());
		return nbt;
	}
	
	@Override
	public void onExplosion(Detonate event) {
		if (event.getExplosion().getIndirectSourceEntity() instanceof ServerPlayer player) {
			player.sendSystemMessage(Component.translatable("caw.message.claim.protected.system.explosion"));
		}
		ClaimData claimData = ClaimDataManager.get().getClaim(claimUUID).orElseThrow(() -> new IllegalStateException("Claim not found"));
		event.getExplosion().getToBlow().removeIf(pos -> {
			return claimData.isIn(event.getLevel(), pos);
		});
		super.onExplosion(event);
	}
	
	@Override
	public void onEntityTickInsideClaim(LivingTickEvent event) {
		super.onEntityTickInsideClaim(event);
	}
	
	@Override
	public void onPlayerEnterClaim(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("caw.message.claim.entered.system"));
		super.onPlayerEnterClaim(player);
	}
	
	@Override
	public void onPlayerLeaveClaim(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("caw.message.claim.exited.system"));
		super.onPlayerLeaveClaim(player);
	}
	
	@Override
	public boolean onMobGriefing(EntityMobGriefingEvent event) {
		return false;
	}
	
	@Override
	public boolean conflictWithFactionClaims() {
		return true;
	}
}
