package net.rainfantasy.claims_and_warfares.common.functionalities.claims.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.DebugClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.DebugFullyProtectedClaim;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.mapper.MapPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import net.rainfantasy.claims_and_warfares.common.utils.MinecraftUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ClaimDataManager extends SavedData {
	
	public static final String DATA_NAME = "caw_claims_data";
	
	private static ClaimDataManager INSTANCE;
	private static MinecraftServer SERVER;
	private final HashMap<String, HashMap<Vector2i, Set<UUID>>> chunkToClaim = new HashMap<>();
	private HashMap<UUID, ClaimData> claims = new HashMap<>();
	private static final HashMap<String, Level> levelCache = new HashMap<>();
	
	private ClaimDataManager() {
		this.setDirty();
	}
	
	private ClaimDataManager(HashMap<UUID, ClaimData> claims) {
		this.claims = claims;
		this.refresh();
	}
	
	private static Vector2i toChunk(BlockPos pos) {
		return new Vector2i(pos.getX() >> 4, pos.getZ() >> 4);
	}
	
	////////
	
	public static ClaimDataManager get() {
		if (INSTANCE == null) {
			throw new RuntimeException("Tried to get data instance before initialization!");
		}
		return INSTANCE;
	}
	
	public static Optional<Level> getLevelById(String id){
		return Optional.ofNullable(levelCache.computeIfAbsent(id,
		(idIn) -> {
			AtomicReference<Level> result = new AtomicReference<>(null);
			SERVER.getAllLevels().forEach(lvl -> {
				if(MinecraftUtils.getLevelId(lvl).equals(idIn)){
					result.set(lvl);
				}
			});
			return result.get();
		}));
	}
	
	public static void init(MinecraftServer _server) {
		if (_server == null) return;
		ClaimDataManager.SERVER = _server;
		
		//Otherwise will error in single player if another save is loaded
		ClaimDataManager.levelCache.clear();
		
		ClaimDataManager.INSTANCE = ClaimDataManager.loadOrCreate(_server);
	}
	
	private static ClaimDataManager loadOrCreate(MinecraftServer server) {
		if (server == null) return new ClaimDataManager();
		ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
		assert overworld != null;
		CAWConstants.LOGGER.info("Loading claim data");
		return overworld.getDataStorage().computeIfAbsent(ClaimDataManager::load, ClaimDataManager::new, DATA_NAME);
	}
	
	public void refresh() {
		this.removeEmptyClaims();
		this.refreshChunkToClaimMap();
		MapPacketGenerator.sendToPlayerWithOpenMapScreen(SERVER);
		ClaimPacketGenerator.sendToPlayerWithOpenMapScreen(SERVER);
		this.setDirty();
	}
	
	
	private void refreshChunkToClaimMap() {
		chunkToClaim.clear();
		claims.forEach((uuid, claimData) -> {
			claimData.claimedChunks.forEach(chunk -> {
				getClaimsIn(claimData.dimension)
					.computeIfAbsent(chunk, k -> new CopyOnWriteArraySet<>()).add(uuid);
			});
		});
	}
	
	private void removeEmptyClaims() {
		claims.entrySet().removeIf(entry -> entry.getValue().claimedChunks.isEmpty());
	}
	
	////////
	
	private Map<Vector2i, Set<UUID>> getClaimsIn(String dimension){
		return this.chunkToClaim.computeIfAbsent(dimension, k -> new HashMap<>());
	}
	
	/**
	 * get the set of claim UUIDs at a given chunk
	 * @param chunk the chunk to check
	 * @return Set of UUID, of the claims at the chunk
	 */
	private Set<UUID> getClaimUUIDAt(String dimensionName, Vector2i chunk) {
		return this.getClaimsIn(dimensionName).computeIfAbsent(chunk, k -> new CopyOnWriteArraySet<>());
	}
	
	/**
	 * get the set of claim UUIDs at a given position
	 * @param pos the position to check
	 * @return Set of UUID, of the claims at the position
	 */
	private Set<UUID> getClaimUUIDAt(Level level, BlockPos pos) {
		return getClaimUUIDAt(MinecraftUtils.getLevelId(level), toChunk(pos));
	}
	
	
	////////
	
	public void debugAddClaimAt(Level level, BlockPos pos) {
		Set<UUID> claimUUIDSet = getClaimUUIDAt(level, pos);
		ClaimData claimData;
		if (claimUUIDSet.isEmpty()) {
			claimData = new ClaimData(level);
			claimData.addClaimFeature(new DebugClaimFeature());
			claimData.addClaimFeature(new DebugFullyProtectedClaim(claimData.claimUUID));
			claims.put(claimData.claimUUID, claimData);
			refreshChunkToClaimMap();
		} else {
			CAWConstants.debugLog("already claimed at {}", pos);
			return;
		}
		claimData.claimedChunks.add(toChunk(pos));
		CAWConstants.debugLog("added claim data: {}", claimData);
		this.refresh();
	}
	
	public void debugRemoveClaimAt(Level level, BlockPos pos) {
		List<UUID> claimUUIDs = getClaimUUIDAt(level, pos).stream().toList();
		if (claimUUIDs.isEmpty()) return;
		for (UUID claimUUID : claimUUIDs) {
			ClaimData claimData = claims.get(claimUUID);
			if (claimData == null) continue;
			removeClaim(claimUUID);
		}
		this.refresh();
	}
	
	////////
	
	public void addClaim(ClaimData claim){
		claims.put(claim.claimUUID, claim);
		this.refresh();
	}
	
	/**
	 * remove a claim by UUID
	 * @param claimUUID the UUID of the claim to remove
	 */
	public void removeClaim(UUID claimUUID) {
		ClaimData claimData = claims.get(claimUUID);
		if (claimData == null) return;
		ClaimEventHandler.fireExitEventOnUnClaim(claimData, SERVER);
		claimData.claimedChunks.forEach(chunk -> getClaimsIn(claimData.dimension).get(chunk).remove(claimUUID));
		claims.remove(claimUUID);
		this.refresh();
	}
	
	/**
	 * check if a claim contains a given position
	 * @param claimUUID the UUID of the claim to check
	 * @param pos the position to check
	 * @return true if the claim contains the position, false otherwise
	 */
	public boolean doesClaimContain(UUID claimUUID, BlockPos pos) {
		ClaimData claimData = claims.get(claimUUID);
		if (claimData == null) return false;
		return claimData.claimedChunks.contains(toChunk(pos));
	}
	
	/**
	 * get the claims at a given chunk
	 * @param chunk the chunk to check
	 * @return List of the claims at the chunk
	 */
	public List<ClaimData> getClaimsAt(String levelName, Vector2i chunk) {
		Set<UUID> claimUUIDs = getClaimsIn(levelName).get(chunk);
		if (claimUUIDs == null) return List.of();
		return claimUUIDs.stream().map(claims::get).toList();
	}
	
	public List<ClaimData> getClaimsAt(Level level, Vector2i chunk) {
		return getClaimsAt(MinecraftUtils.getLevelId(level), chunk);
	}
	
	/**
	 * get the claims at a given position
	 * @param pos the position to check
	 * @return List of the claims at the position
	 */
	public List<ClaimData> getClaimsAt(Level level, BlockPos pos) {
		return getClaimsAt(MinecraftUtils.getLevelId(level), new Vector2i(pos.getX() >> 4, pos.getZ() >> 4));
	}
	
	public List<ClaimData> getClaimsAt(LevelAccessor level, BlockPos pos) {
		return getClaimsAt(MinecraftUtils.getLevelId(level), new Vector2i(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public List<UUID> getClaimUUIDsInZone(Level level, Vector2i min, Vector2i max) {
		String levelId = MinecraftUtils.getLevelId(level);
		List<UUID> claimUUIDs = new ArrayList<>();
		CoordUtil.iterateCoords(min, max).forEach(chunk -> claimUUIDs.addAll(getClaimUUIDAt(levelId, chunk)));
		return claimUUIDs;
	}
	
	/**
	 * get the claim data by UUID
	 * @param claimUUID the UUID of the claim to get
	 * @return the claim data, or empty if the claim does not exist
	 */
	public Optional<ClaimData> getClaim(UUID claimUUID) {
		return Optional.ofNullable(claims.get(claimUUID));
	}
	
	/**
	 * get all the claim UUIDs
	 * @return List of all the claim UUIDs
	 */
	public List<UUID> getAllClaimUUIDs() {
		return claims.keySet().stream().toList();
	}
	
	public List<ClaimData> getAllClaims() {
		return new ArrayList<>(claims.values());
	}
	
	////////
	
	public static ClaimDataManager load(CompoundTag tag) {
		ListTag claimList = tag.getList("claims", ListTag.TAG_COMPOUND);
		HashMap<UUID, ClaimData> claims = new HashMap<>();
		for (int i = 0; i < claimList.size(); i++) {
			CompoundTag claimTag = claimList.getCompound(i);
			ClaimData claimData = new ClaimData().readFromNBT(claimTag);
			claims.put(claimData.claimUUID, claimData);
		}
		return new ClaimDataManager(claims);
	}
	
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
		CAWConstants.LOGGER.info("saving claim data");
		ListTag claimList = new ListTag();
		claims.forEach((uuid, claimData) -> {
			CompoundTag claimTag = claimData.writeToNBT(new CompoundTag());
			CAWConstants.debugLog("saving claim data: {}", claimTag);
			claimList.add(claimTag);
		});
		nbt.put("claims", claimList);
		return nbt;
	}
	
}
