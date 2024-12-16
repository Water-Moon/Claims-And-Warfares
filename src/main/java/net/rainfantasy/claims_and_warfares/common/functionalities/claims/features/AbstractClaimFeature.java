package net.rainfantasy.claims_and_warfares.common.functionalities.claims.features;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;

import java.util.UUID;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public abstract class AbstractClaimFeature implements ISerializableNBTData<AbstractClaimFeature, CompoundTag> {
	
	UUID featureUUID = UUID.randomUUID();
	
	public UUID getUUID() {
		return featureUUID;
	}
	
	public abstract String getName();
	
	public abstract ResourceLocation getRegistryName();
	
	@Override
	public String toString() {
		return "AbstractClaimFeature{" +
		       "featureUUID=" + featureUUID +
		       '}';
	}
	
	/**
	 * Called when an entity tries to place a block in a claimed chunk
	 *
	 * @param event The event
	 * @return False if the entity should not be able to place the block
	 */
	public boolean onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		return true;
	}
	
	/**
	 * Called when an entity tries to place a block in a claimed chunk
	 *
	 * @param event The event
	 * @return False if the entity should not be able to place the block
	 */
	public boolean onBreakBlock(BlockEvent.BreakEvent event) {
		return true;
	}
	
	/**
	 * Called when a player tries to interact using an item in a claimed chunk
	 * (Specifically, right-click)
	 *
	 * @param event The event
	 * @return False if the player should not be able to interact with the block
	 */
	public boolean onInteractBlock(RightClickBlock event) {
		return true;
	}
	
	/**
	 * Called when an entity tries to trample farmland in a claimed chunk
	 *
	 * @param event The event
	 * @return False if the entity should not be able to trample the farmland
	 */
	public boolean onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		return true;
	}
	
	/**
	 * Called when an entity is attacked in a claimed chunk
	 *
	 * @param event The event
	 * @return False if the source entity should not be able to attack the target entity
	 */
	public boolean onEntityAttacked(LivingAttackEvent event) {
		return true;
	}
	
	/**
	 * Called when something is about to explode in a claimed chunk
	 * Use this to e.g. cancel the explosion
	 *
	 * @param event The event
	 */
	public void onExplosion(ExplosionEvent.Detonate event) {
		//for example: event.getExplosion().clearToBlow();
	}
	
	/**
	 * Called each game tick for each entity inside the claim
	 * Use this to e.g. apply effect, damage, etc.
	 *
	 * @param event The event
	 */
	public void onEntityTickInsideClaim(LivingTickEvent event) {
		//for example: event.getEntity().addEffect(new MobEffectInstance(MobEffects.GLOWING));
	}
	
	/**
	 * Called once when a player enters (or teleports into/log in at/respawns in) a claim<br>
	 * i.e. all cases where the player was not in the claim before <br>
	 * Only for players due to costly operations
	 *
	 * @param player The player
	 */
	public void onPlayerEnterClaim(ServerPlayer player) {
	
	}
	
	/**
	 * Called once when a player leaves (or teleports out of/died in) a claim<br>
	 * i.e. all cases where the player was in the claim before <br>
	 * Only for players due to costly operations
	 *
	 * @param player The player
	 */
	public void onPlayerLeaveClaim(ServerPlayer player) {
	
	}
	
	/**
	 * Called when checking if a claim should be removed <br>
	 * If at least one feature returns true, the claim will be removed
	 *
	 * @return True if the claim is invalid
	 */
	public boolean isInvalid() {
		return false;
	}
	
	protected abstract AbstractClaimFeature readNBT(CompoundTag nbt);
	
	protected abstract CompoundTag writeNBT(CompoundTag nbt);
	
	
	@Override
	public AbstractClaimFeature readFromNBT(CompoundTag nbt) {
		CompoundTag innerTag = nbt.getCompound("data");
		AbstractClaimFeature result = readNBT(innerTag);
		result.featureUUID = nbt.getString("uuid").isEmpty() ? UUID.randomUUID() : UUID.fromString(nbt.getString("uuid"));
		return result;
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putString("uuid", featureUUID.toString());
		CompoundTag innerTag = writeNBT(new CompoundTag());
		nbt.put("data", innerTag);
		return nbt;
	}
	
}
