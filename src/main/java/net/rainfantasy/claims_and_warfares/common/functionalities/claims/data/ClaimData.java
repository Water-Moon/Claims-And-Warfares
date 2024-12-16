package net.rainfantasy.claims_and_warfares.common.functionalities.claims.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.ClaimFeatureLoader;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.ColoredClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.ISerializableNBTData;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.SerializableDateTime;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClaimData implements ISerializableNBTData<ClaimData, CompoundTag> {
	
	public static final String NBT_CREATION_TIME = "CreationTime";
	public static final String NBT_CLAIM_DIMENSION = "level";
	public static final String NBT_CLAIMED_CHUNKS = "ClaimedChunks";
	public static final String NBT_CLAIM_FEATURES = "ClaimFeatures";
	public static final String NBT_CLAIM_UUID = "ClaimUUID";
	
	
	SerializableDateTime creationTime;
	Set<Vector2i> claimedChunks = new CopyOnWriteArraySet<>();
	String dimension;
	Set<AbstractClaimFeature> claimFeatures = new CopyOnWriteArraySet<>();
	UUID claimUUID;
	
	//for loading from nbt only
	public ClaimData() {
		this.creationTime = new SerializableDateTime();
		claimUUID = UUID.randomUUID();
	}
	
	//use this one
	public ClaimData(Level level) {
		this();
		this.dimension = level.dimension().location().toString();
	}
	
	private ClaimData(SerializableDateTime creationTime, String dimension, Set<Vector2i> claimedChunks, Set<AbstractClaimFeature> claimFeatures, UUID claimUUID) {
		this.creationTime = creationTime;
		this.dimension = dimension;
		this.claimedChunks = claimedChunks;
		this.claimFeatures = claimFeatures;
		this.claimUUID = claimUUID;
	}
	
	public UUID getUUID() {
		return claimUUID;
	}
	
	public void claimChunk(Vector2i chunk) {
		this.claimedChunks.add(chunk);
	}
	
	public boolean isChunkClaimed(String dimension, Vector2i chunk) {
		if (!this.dimension.equals(dimension)) return false;
		return this.claimedChunks.contains(chunk);
	}
	
	public boolean isIn(@NotNull Level level, @NotNull BlockPos pos) {
		String dimensionId = level.dimension().location().toString();
		return this.isChunkClaimed(dimensionId, new Vector2i(pos.getX() >> 4, pos.getZ() >> 4));
	}
	
	public Optional<AbstractClaimFeature> getFeature(Class<? extends AbstractClaimFeature> clazz) {
		return this.getFeatures(clazz).stream().findFirst();
	}
	
	public boolean hasFeature(Class<? extends AbstractClaimFeature> clazz) {
		return this.claimFeatures.stream().anyMatch(clazz::isInstance);
	}
	
	public List<AbstractClaimFeature> getFeatures(Class<? extends AbstractClaimFeature> clazz) {
		return this.claimFeatures.stream().filter(clazz::isInstance).toList();
	}
	
	public boolean isInvalid() {
		return this.claimFeatures.stream().anyMatch(feature -> {
			if (feature == null) {
				throw new RuntimeException("Null feature in claim data " + this);
			}
			if (feature.isInvalid()) {
				CAWConstants.debugLog("Claim {} invalidated by feature {}", claimUUID, feature.toString());
			}
			return feature.isInvalid();
		});
	}
	
	public void addClaimFeature(AbstractClaimFeature feature) {
		this.claimFeatures.add(feature);
	}
	
	public void removeClaimFeature(UUID featureUUID) {
		this.claimFeatures.removeIf(feature -> feature.getUUID().equals(featureUUID));
	}
	
	////////
	
	public int getClaimColor() {
		if (this.getFeature(ColoredClaimFeature.class).isPresent()) {
			return ((ColoredClaimFeature) this.getFeature(ColoredClaimFeature.class).get()).getColor();
		}
		if (this.getFeature(FactionOwnedClaimFeature.class).isPresent()) {
			UUID factionUUID = ((FactionOwnedClaimFeature) this.getFeature(FactionOwnedClaimFeature.class).get()).getFactionUUID();
			return FactionDataManager.get().getFaction(factionUUID).map(FactionData::getFactionColor).orElse(ColorUtil.combine(255, 127, 0, 0));
		}
		return ColorUtil.combine(255, 127, 0, 0);
	}
	
	public String getDimensionID() {
		return this.dimension;
	}
	
	////////
	
	@Override
	public ClaimData readFromNBT(CompoundTag nbt) {
		
		CompoundTag timeTag = nbt.getCompound(NBT_CREATION_TIME);
		SerializableDateTime creationTime = new SerializableDateTime().readFromNBT(timeTag);
		
		String dimension = nbt.getString(NBT_CLAIM_DIMENSION);
		
		Set<Vector2i> claimedChunksSet = new HashSet<>();
		ListTag claimedChunksTag = nbt.getList(NBT_CLAIMED_CHUNKS, ListTag.TAG_COMPOUND);
		for (int i = 0; i < claimedChunksTag.size(); i++) {
			CompoundTag chunkTag = claimedChunksTag.getCompound(i);
			int x = chunkTag.getInt("x");
			int y = chunkTag.getInt("y");
			claimedChunksSet.add(new Vector2i(x, y));
		}
		Set<Vector2i> claimedChunks = new CopyOnWriteArraySet<>(claimedChunksSet);
		
		ListTag claimFeaturesTag = nbt.getList(NBT_CLAIM_FEATURES, ListTag.TAG_COMPOUND);
		Set<AbstractClaimFeature> claimFeaturesSet = new HashSet<>();
		for (int i = 0; i < claimFeaturesTag.size(); i++) {
			CompoundTag featureTag = claimFeaturesTag.getCompound(i);
			AbstractClaimFeature feature = ClaimFeatureLoader.read(featureTag);
			claimFeaturesSet.add(feature);
		}
		Set<AbstractClaimFeature> claimFeatures = new CopyOnWriteArraySet<>(claimFeaturesSet);
		
		UUID claimUUID = UUID.fromString(nbt.getString(NBT_CLAIM_UUID));
		
		return new ClaimData(creationTime, dimension, claimedChunks, claimFeatures, claimUUID);
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		CompoundTag timeTag = this.creationTime.writeToNBT(new CompoundTag());
		nbt.put(NBT_CREATION_TIME, timeTag);
		
		nbt.putString(NBT_CLAIM_DIMENSION, this.dimension);
		
		ListTag claimedChunksTag = new ListTag();
		for (Vector2i chunk : this.claimedChunks) {
			CompoundTag chunkTag = new CompoundTag();
			chunkTag.putInt("x", chunk.x);
			chunkTag.putInt("y", chunk.y);
			claimedChunksTag.add(chunkTag);
		}
		nbt.put(NBT_CLAIMED_CHUNKS, claimedChunksTag);
		
		ListTag claimFeaturesTag = new ListTag();
		for (AbstractClaimFeature feature : this.claimFeatures) {
			if (feature == null) continue;
			claimFeaturesTag.add(ClaimFeatureLoader.write(feature, new CompoundTag()));
		}
		nbt.put(NBT_CLAIM_FEATURES, claimFeaturesTag);
		
		nbt.putString(NBT_CLAIM_UUID, this.claimUUID.toString());
		return nbt;
	}
	
	@Override
	public String toString() {
		return "ClaimData{" +
		       "creationTime=" + creationTime +
		       ", dimension='" + dimension + '\'' +
		       ", claimedChunks=" + claimedChunks +
		       ", claimFeatures=" + claimFeatures +
		       ", claimUUID=" + claimUUID +
		       '}';
	}
	
	public boolean isEntityIn(LivingEntity entity) {
		return this.isIn(entity.level(), entity.blockPosition());
	}
	
	public Optional<AbstractClaimFeature> getFeature(UUID featureUUID) {
		return this.claimFeatures.stream().filter(feature -> feature.getUUID().equals(featureUUID)).findFirst();
	}
	
	public Optional<AbstractClaimFeature> getFeatureCheckType(UUID featureUUID, Class<? extends AbstractClaimFeature> clazz) {
		return this.claimFeatures.stream().filter(feature -> feature.getUUID().equals(featureUUID) && clazz.isInstance(feature)).findFirst();
	}
}
