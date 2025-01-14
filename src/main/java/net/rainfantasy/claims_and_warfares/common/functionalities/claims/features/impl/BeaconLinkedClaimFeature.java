package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.TagRegistry;
import net.rainfantasy.claims_and_warfares.common.setups.registries.TagRegistry.Blocks;

import java.util.Optional;
import java.util.UUID;

import static net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature.BREAK_PLACE;
import static net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature.INTERACT;

public class BeaconLinkedClaimFeature extends AbstractClaimFeature {
	
	BlockPos beaconPos;
	UUID claimUUID;
	UUID linkedFactionClaimFeatureUUID;
	boolean enabled = true;
	boolean protectInteraction = true;
	boolean protectMobGriefing = false;
	boolean protectExplosions = false;
	int size;
	
	public BeaconLinkedClaimFeature() {
	}
	
	public BeaconLinkedClaimFeature(BlockPos beaconPos, UUID claimUUID, int size) {
		this.beaconPos = beaconPos;
		this.claimUUID = claimUUID;
		this.size = size;
	}
	
	public Optional<ClaimBeaconBlockEntity> getBeacon() {
		Optional<ClaimData> data = ClaimDataManager.get().getClaim(this.claimUUID);
		if (data.isEmpty()) return Optional.empty();
		String dimension = data.get().getDimensionID();
		return ClaimDataManager.getLevelById(dimension)
		       .map(lvl -> lvl.getBlockEntity(this.beaconPos))
		       .map(be -> be instanceof ClaimBeaconBlockEntity ? (ClaimBeaconBlockEntity) be : null);
	}
	
	@Override
	public boolean isInvalid() {
		if (this.getBeacon().isEmpty()) return true;
		ClaimBeaconBlockEntity beacon = this.getBeacon().get();
		if (!beacon.shouldMaintainClaim()) return true;
		return super.isInvalid();
	}
	
	@Override
	public String getName() {
		return "Beacon Linked Claim";
	}
	
	@Override
	public ResourceLocation getRegistryName() {
		return CAWConstants.rl("beacon_linked_claim");
	}
	
	@Override
	protected AbstractClaimFeature readNBT(CompoundTag nbt) {
		int[] pos = nbt.getIntArray("pos");
		UUID claimUUID = UUID.fromString(nbt.getString("claimUUID"));
		int size = nbt.getInt("size");
		boolean enabled = nbt.getBoolean("enabled");
		boolean protectInteraction = nbt.getBoolean("protectInteraction");
		boolean protectMobGriefing = nbt.getBoolean("protectMobGriefing");
		boolean protectExplosions = nbt.getBoolean("protectExplosions");
		BeaconLinkedClaimFeature feature = new BeaconLinkedClaimFeature(new BlockPos(pos[0], pos[1], pos[2]), claimUUID, size);
		feature.enabled = enabled;
		feature.protectInteraction = protectInteraction;
		feature.protectMobGriefing = protectMobGriefing;
		feature.protectExplosions = protectExplosions;
		return feature;
	}
	
	@Override
	protected CompoundTag writeNBT(CompoundTag nbt) {
		nbt.putIntArray("pos", new int[]{beaconPos.getX(), beaconPos.getY(), beaconPos.getZ()});
		nbt.putString("claimUUID", this.claimUUID.toString());
		nbt.putInt("size", this.size);
		nbt.putBoolean("enabled", this.enabled);
		nbt.putBoolean("protectInteraction", this.protectInteraction);
		nbt.putBoolean("protectMobGriefing", this.protectMobGriefing);
		nbt.putBoolean("protectExplosions", this.protectExplosions);
		return nbt;
	}
	
	public UUID getClaimUUID() {
		return claimUUID;
	}
	
	public BlockPos getBeaconPos() {
		return beaconPos;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return "BeaconLinkedClaimFeature{" +
		       "beaconPos=" + beaconPos +
		       ", claimUUID=" + claimUUID +
		       ", size=" + size +
		       '}';
	}
	
	private Optional<FactionOwnedClaimFeature> getLinkedClaimFeature() {
		Optional<ClaimData> linkedClaimData = ClaimDataManager.get().getClaim(this.claimUUID);
		if (linkedClaimData.isEmpty()) return Optional.empty();
		return linkedClaimData.map(data -> {
			AbstractClaimFeature feature = data.getFeature(FactionOwnedClaimFeature.class).orElse(null);
			if (feature instanceof FactionOwnedClaimFeature factionOwnedClaimFeature) {
				return factionOwnedClaimFeature;
			}
			return null;
		});
	}
	
	private String getLinkedFactionName() {
		return this.getLinkedClaimFeature().map(factionOwnedClaimFeature -> {
			return FactionDataManager.get().getFactionName(factionOwnedClaimFeature.getFactionUUID());
		}).orElse("Unknown");
	}
	
	private boolean checkAllowed(Entity entity, int type) {
		//only protect against players
		if (entity == null) return true;
		if (!enabled) return true;
		if (!(entity instanceof ServerPlayer player)) return (!this.protectMobGriefing);
		return (this.getLinkedClaimFeature().map(factionOwnedClaimFeature -> factionOwnedClaimFeature.checkAllowed(player, type)).orElse(true));
	}
	
	
	@Override
	public boolean onPlaceBlock(EntityPlaceEvent event) {
		if (event.getPlacedBlock().is(Blocks.ALLOW_PLACE)) return super.onPlaceBlock(event);
		if (checkAllowed(event.getEntity(), BREAK_PLACE)) return super.onPlaceBlock(event);
		String factionName = getLinkedFactionName();
		event.getEntity().sendSystemMessage(
		Component.translatable("caw.message.claim.protected.faction.place", factionName, event.getPlacedBlock().getBlock().getName()));
		return false;
	}
	
	@Override
	public boolean onBreakBlock(BreakEvent event) {
		if (event.getState().is(TagRegistry.Blocks.ALLOW_BREAK)) return super.onBreakBlock(event);
		if (checkAllowed(event.getPlayer(), BREAK_PLACE)) return super.onBreakBlock(event);
		String factionName = getLinkedFactionName();
		event.getPlayer().sendSystemMessage(
		Component.translatable("caw.message.claim.protected.faction.break",
		factionName, event.getState().getBlock().getName()));
		return false;
	}
	
	@Override
	public boolean onInteractBlock(RightClickBlock event) {
		if (!protectInteraction) return super.onInteractBlock(event);
		
		BlockState state = event.getEntity().level().getBlockState(event.getPos());
		if (state.is(TagRegistry.Blocks.ALLOW_INTERACT)) return super.onInteractBlock(event);
		if (checkAllowed(event.getEntity(), INTERACT)) return super.onInteractBlock(event);
		String factionName = getLinkedFactionName();
		event.getEntity().sendSystemMessage(
		Component.translatable("caw.message.claim.protected.faction.interact",
		factionName, state.getBlock().getName()));
		return false;
	}
	
	@Override
	public boolean onFarmlandTrample(FarmlandTrampleEvent event) {
		if (checkAllowed(event.getEntity(), BREAK_PLACE)) return super.onFarmlandTrample(event);
		String factionName = getLinkedFactionName();
		event.getEntity().sendSystemMessage(
		Component.translatable("caw.message.claim.protected.faction.trample",
		factionName, event.getEntity().level().getBlockState(event.getPos()).getBlock().getName()));
		return false;
	}
	
	@Override
	public boolean onEntityAttacked(LivingAttackEvent event) {
//		if(event.getEntity() instanceof Player) return true;    //Always allow PVP
//		if(checkAllowed(event.getSource().getEntity())) return super.onEntityAttacked(event);
//		String factionName = getLinkedFactionName();
//		event.getSource().getEntity().sendSystemMessage
//		                             (Component.translatable("caw.message.claim.protected.faction.attack", factionName, event.getEntity().getDisplayName()));
//		return false;
		return true;
	}
	
	@Override
	public void onPlayerEnterClaim(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("caw.message.claim.entered.faction", getLinkedFactionName()));
		super.onPlayerEnterClaim(player);
	}
	
	@Override
	public void onPlayerLeaveClaim(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("caw.message.claim.exited.faction", getLinkedFactionName()));
		super.onPlayerLeaveClaim(player);
	}
	
	public void setMinAllowedDiplomaticLevel(int minAllowedDiplomaticLevel, int type) {
		this.getLinkedClaimFeature().ifPresent(factionOwnedClaimFeature -> {
			if (type == BREAK_PLACE) {
				factionOwnedClaimFeature.minAllowedDiplomaticLevelBreakPlace = minAllowedDiplomaticLevel;
			} else if (type == FactionOwnedClaimFeature.INTERACT) {
				factionOwnedClaimFeature.minAllowedDiplomaticLevelInteract = minAllowedDiplomaticLevel;
			} else {
				throw new IllegalArgumentException("Unknown interaction type " + type);
			}
		});
	}
	
	@Override
	public void onExplosion(Detonate event) {
		if (!protectExplosions) return;
		if (event.getExplosion().getIndirectSourceEntity() instanceof ServerPlayer player) {
			player.sendSystemMessage(Component.translatable("caw.message.claim.protected.faction.explosion", getLinkedFactionName()));
		}
		ClaimData claimData = ClaimDataManager.get().getClaim(claimUUID).orElseThrow(() -> new IllegalStateException("Claim not found"));
		event.getExplosion().getToBlow().removeIf(pos -> claimData.isIn(event.getLevel(), pos));
		super.onExplosion(event);
	}
	
	@Override
	public boolean onMobGriefing(EntityMobGriefingEvent event) {
		if (protectMobGriefing) return false;
		return super.onMobGriefing(event);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setProtectMobGriefing(boolean protectMobGriefing) {
		this.protectMobGriefing = protectMobGriefing;
	}
	
	public void setProtectExplosions(boolean protectExplosions) {
		this.protectExplosions = protectExplosions;
	}
	
	public void setProtectInteraction(boolean protectInteraction) {
		this.protectInteraction = protectInteraction;
	}
}
