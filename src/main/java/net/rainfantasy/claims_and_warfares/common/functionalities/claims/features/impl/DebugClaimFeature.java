package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;

public class DebugClaimFeature extends AbstractClaimFeature {
	
	public DebugClaimFeature() {
	}
	
	@Override
	public String getName() {
		return "Debug";
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return CAWConstants.rl("debug");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		assert nbt.getString("test").equals("test");
		return new DebugClaimFeature();
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putString("test", "test");
		return nbt;
	}
	
	@Override
	public boolean onPlaceBlock(EntityPlaceEvent event) {
		if (event.getEntity() == null) return true;
		CAWConstants.debugLog("{} placed {} in claimed chunk", event.getEntity().getName(), event.getPlacedBlock().getBlock().getName());
		return true;
	}
	
	@Override
	public boolean onBreakBlock(BreakEvent event) {
		if (event.getPlayer() == null) return true;
		CAWConstants.debugLog("{} broke {} in claimed chunk", event.getPlayer().getName(), event.getState().getBlock().getName());
		return true;
	}
	
	@Override
	public boolean onInteractBlock(RightClickBlock event) {
		if (event.getEntity() == null) return true;
		CAWConstants.debugLog("{} interacted with {} in claimed chunk", event.getEntity().getName(), event.getLevel().getBlockState(event.getPos()).getBlock().getName());
		return true;
	}
	
	@Override
	public boolean onFarmlandTrample(FarmlandTrampleEvent event) {
		if (event.getEntity() == null) return true;
		CAWConstants.debugLog("{} trampled farmland at {} in claimed chunk", event.getEntity().getName(), event.getPos());
		return true;
	}
	
	@Override
	public void onExplosion(Detonate event) {
		CAWConstants.debugLog("Explosion at {} in claimed chunk", event.getExplosion().getPosition());
	}
	
	@Override
	public void onEntityTickInsideClaim(LivingTickEvent event) {
		//CAWConstants.debugLog("{} is inside claimed chunk", event.getEntity().getName());
	}
	
	@Override
	public void onPlayerEnterClaim(ServerPlayer player) {
		CAWConstants.debugLog("{} entered claimed chunk", player.getName());
	}
	
	@Override
	public void onPlayerLeaveClaim(ServerPlayer player) {
		CAWConstants.debugLog("{} left claimed chunk", player.getName());
	}
}
