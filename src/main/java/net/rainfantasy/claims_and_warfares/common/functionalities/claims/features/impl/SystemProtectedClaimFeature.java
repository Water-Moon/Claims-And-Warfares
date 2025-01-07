package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.setups.registries.TagRegistry;
import net.rainfantasy.claims_and_warfares.common.setups.registries.TagRegistry.Blocks;

import java.util.UUID;

public class SystemProtectedClaimFeature extends AbstractClaimFeature {
	
	UUID claimUUID;
	
	public SystemProtectedClaimFeature() {
		this(UUID.randomUUID());
	}
	
	public SystemProtectedClaimFeature(UUID claimUUID) {
		this.claimUUID = claimUUID;
	}
	
	@Override
	public String getName() {
		return "System Claim";
	}
	
	@Override
	public Component getDisplayName() {
		return Component.translatable("caw.gui.label.claim_system");
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return new ResourceLocation("caw", "system_claim");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		UUID claimUUID = UUID.fromString(nbt.getString("uuid"));
		return new SystemProtectedClaimFeature(claimUUID);
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putString("uuid", claimUUID.toString());
		return nbt;
	}
	
	@Override
	public boolean onPlaceBlock(EntityPlaceEvent event) {
		if (event.getPlacedBlock().is(Blocks.ALLOW_PLACE)) return super.onPlaceBlock(event);
		if (event.getEntity() instanceof ServerPlayer player) {
			player.sendSystemMessage(Component.translatable("caw.message.claim.protected.system.place", event.getPlacedBlock().getBlock().getName()));
			return false;
		}
		return super.onPlaceBlock(event);
	}
	
	@Override
	public boolean onBreakBlock(BreakEvent event) {
		if (event.getState().is(TagRegistry.Blocks.ALLOW_BREAK)) return super.onBreakBlock(event);
		if (event.getPlayer() instanceof ServerPlayer player) {
			player.sendSystemMessage(Component.translatable("caw.message.claim.protected.system.break",
			player.level().getBlockState(event.getPos()).getBlock().getName()));
			return false;
		}
		return super.onBreakBlock(event);
	}
	
	@Override
	public boolean onInteractBlock(RightClickBlock event) {
		return super.onInteractBlock(event);
	}
	
	@Override
	public boolean onFarmlandTrample(FarmlandTrampleEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.sendSystemMessage(Component.translatable("caw.message.claim.protected.system.trample",
			player.level().getBlockState(event.getPos()).getBlock().getName()));
			return false;
		}
		return super.onFarmlandTrample(event);
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
	public boolean onEntityAttacked(LivingAttackEvent event) {
//		if (Optional.ofNullable(event.getSource().getEntity()).map(e -> e instanceof ServerPlayer).orElse(false)) {
//			event.getSource().getEntity().sendSystemMessage(Component.translatable("caw.message.claim.protected.system.attack",
//			event.getEntity().getDisplayName()));
//			return false;
//		}
		return super.onEntityAttacked(event);
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
