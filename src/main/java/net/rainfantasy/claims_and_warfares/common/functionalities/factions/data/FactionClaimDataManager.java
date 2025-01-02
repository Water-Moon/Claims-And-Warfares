package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.FakePlayer;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("FieldCanBeLocal")
public class FactionClaimDataManager extends SavedData {
	
	public static final String DATA_NAME = "caw_faction_claim_data";
	
	//faction -> claim
	ConcurrentHashMap<UUID, CopyOnWriteArraySet<UUID>> factionToClaimMap = new ConcurrentHashMap<>();
	private static FactionClaimDataManager INSTANCE;
	private static MinecraftServer SERVER = null;
	
	
	public FactionClaimDataManager() {
		this.setDirty();
	}
	
	public static FactionClaimDataManager get() {
		if (INSTANCE == null) {
			throw new RuntimeException("Tried to get data instance before initialization!");
		}
		return INSTANCE;
	}
	
	public static void init(MinecraftServer server) {
		if (server == null) return;
		SERVER = server;
		INSTANCE = loadOrCreate(server);
	}
	
	private void reAddAllClaims() {
		ConcurrentHashMap<UUID, CopyOnWriteArraySet<UUID>> newFactionToClaimMap = new ConcurrentHashMap<>();
		List<ClaimData> effectivelyFinalClaimData = Collections.unmodifiableList(ClaimDataManager.get().getAllClaims());
		
		FactionDataManager.get().getAllFactions()
		.parallelStream()
		.forEach(faction -> {
			
			List<UUID> claims = new ArrayList<>();
			effectivelyFinalClaimData.forEach(claim -> {
				//if claim is faction owned and this faction owns it
				if (claim.getFeatures(FactionOwnedClaimFeature.class)
				    .stream()
				    .anyMatch(feature -> {
					    FactionOwnedClaimFeature factionOwnedClaimFeature = (FactionOwnedClaimFeature) feature;
					    return faction.is(factionOwnedClaimFeature.getFactionUUID());
				    })
				) {
					claims.add(claim.getUUID());
				}
			});
			newFactionToClaimMap.put(faction.getFactionUUID(), new CopyOnWriteArraySet<>(claims));
		});
		this.factionToClaimMap = new ConcurrentHashMap<>(newFactionToClaimMap);
	}
	
	@SuppressWarnings("RedundantIfStatement")
	private void validateClaims() {
		factionToClaimMap.forEach((key, value) -> value.removeIf(claim -> {
			Optional<ClaimData> claimData = ClaimDataManager.get().getClaim(claim);
			//1. if claim doesn't exist
			if (claimData.isEmpty()) return true;
			//2. if claim isn't a faction owned claim
			if (claimData.get().getFeatures(FactionOwnedClaimFeature.class).isEmpty()) return true;
			//3. if the claim isn't this faction's
			if (claimData.get().getFeatures(FactionOwnedClaimFeature.class)
			    .stream()
			    .noneMatch(feature -> ((FactionOwnedClaimFeature) feature).getFactionUUID().equals(key))    //feature UUID is the faction UUID
			) {
				return true;
			}
			return false;
		}));
		factionToClaimMap.values().removeIf(Set::isEmpty);
	}
	
	private void validateFactions() {
		List<UUID> factionsToRemove = new ArrayList<>();
		factionToClaimMap.keySet().forEach(factionUUID -> {
			if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
				factionsToRemove.add(factionUUID);
			}
		});
		factionsToRemove.forEach(uuid -> factionToClaimMap.remove(uuid).forEach(claim -> ClaimDataManager.get().removeClaim(claim)));
	}
	
	private void removeUnownedFactionClaim() {
		List<ClaimData> effectivelyFinalClaimData = Collections.unmodifiableList(ClaimDataManager.get().getAllClaims());
		effectivelyFinalClaimData.forEach(entry -> {
			if (entry.hasFeature(FactionOwnedClaimFeature.class)) {
				UUID factionUUID = ((FactionOwnedClaimFeature) entry.getFeature(FactionOwnedClaimFeature.class).orElseThrow()).getFactionUUID();
				FactionDataManager.get().getFaction(factionUUID).ifPresentOrElse(f -> {
				}, () -> {
					ClaimDataManager.get().removeClaim(entry.getUUID());
				});
			}
		});
	}
	
	public void refresh(boolean full) {
		this.removeUnownedFactionClaim();
		if (full) {
			this.reAddAllClaims();
		} else {
			this.validateClaims();
		}
		this.validateFactions();
		this.setDirty();
	}
	
	private static FactionClaimDataManager loadOrCreate(MinecraftServer server) {
		if (server == null) return new FactionClaimDataManager();
		ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
		assert overworld != null;
		return overworld.getDataStorage().computeIfAbsent(FactionClaimDataManager::load, FactionClaimDataManager::new, DATA_NAME);
	}
	
	@Override
	public @NotNull CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();
		factionToClaimMap.forEach((faction, claim) -> {
			CompoundTag entry = new CompoundTag();
			entry.putString("faction", faction.toString());
			ListTag claimList = new ListTag();
			claim.forEach(claimUUID -> claimList.add(StringTag.valueOf(claimUUID.toString())));
			entry.put("claims", claimList);
			list.add(entry);
		});
		tag.put("factionToClaimMap", list);
		return tag;
	}
	
	public static FactionClaimDataManager load(CompoundTag tag) {
		FactionClaimDataManager data = new FactionClaimDataManager();
		ListTag list = tag.getList("factionToClaimMap", Tag.TAG_COMPOUND);
		list.forEach(entry -> {
			CompoundTag compound = (CompoundTag) entry;
			UUID faction = UUID.fromString(compound.getString("faction"));
			ListTag claimList = compound.getList("claims", Tag.TAG_STRING);
			List<UUID> claims = new ArrayList<>();
			claimList.forEach(claim -> claims.add(UUID.fromString(claim.getAsString())));
			data.factionToClaimMap.put(faction, new CopyOnWriteArraySet<>(claims));
		});
		data.refresh(true);
		return data;
	}
	
	public Set<UUID> getClaimsOf(UUID faction) {
		return factionToClaimMap.getOrDefault(faction, new CopyOnWriteArraySet<>());
	}
	
	public void addClaimTo(UUID faction, UUID claim) {
		factionToClaimMap.computeIfAbsent(faction, k -> new CopyOnWriteArraySet<>()).add(claim);
		refresh(false);
	}
	
	public void removeClaimOf(UUID faction, UUID claim) {
		factionToClaimMap.computeIfAbsent(faction, k -> new CopyOnWriteArraySet<>()).remove(claim);
		refresh(false);
	}
	
	public static boolean isAuthorizedInFactionClaim(ServerPlayer player, UUID faction, int minDiplomaticLevel) {
		/*
		 * Allowed if:
		 * 0. special check for fake player
		 * 1. is same faction
		 * 2. player is server operator
		 * 3. player is in a faction that is allied with the faction
		 */
		
		if (FactionDataManager.get().getFaction(faction).isEmpty()) {
			CAWConstants.LOGGER.warn("Faction {} doesn't exist!", faction);
			CAWConstants.logStackTrace();
			return true;
		}
		
		//0. Fake player: check faction fake player policy
		if (player instanceof FakePlayer) {
			int fakePlayerPolicy = FactionDataManager.get().getFaction(faction).get().getFakePlayerPolicy();
			if (fakePlayerPolicy == FactionData.FAKE_PLAYER_POLICY_DENY) return false;
			if (fakePlayerPolicy == FactionData.FAKE_PLAYER_POLICY_ALLOW) return true;
			//default is to check like regular player
		}
		
		//1. is same faction
		if (FactionDataManager.get().isPlayerInFaction(player.getUUID(), faction)) return true;
		
		//2. is OP
		//replaced by bypass command
//		if (player.hasPermissions(4)) return true;
		
		//3. is allied
		AtomicBoolean flag = new AtomicBoolean(false);
		FactionDataManager.get().getFaction(faction).get().getDiplomaticRelationships().forEach((key, value) -> {
			if (value.getRelationship() >= minDiplomaticLevel) {
				if (FactionDataManager.get().isPlayerInFaction(player.getUUID(), key)) {
					flag.set(true);
				}
			}
		});
		return flag.get();
	}
}
