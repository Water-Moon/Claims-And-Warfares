package net.rainfantasy.claims_and_warfares.client;

import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientPlayerData;
import net.rainfantasy.claims_and_warfares.client.data_types.OfflinePlayerData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.PTCDiplomaticRelationshipData;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CAWClientDataManager {
	
	public static final Map<UUID, ClientPlayerData> playerData = Collections.synchronizedMap(new HashMap<>());
	public static final Map<UUID, ClientFactionData> factionData = Collections.synchronizedMap(new HashMap<>());
	public static final Map<UUID, Integer> diplomaticRelationshipDataOfSelectedFaction = Collections.synchronizedMap(new HashMap<>());
	public static final Map<UUID, InvitationInfo> knownInvitations = Collections.synchronizedMap(new HashMap<>());
	public static final Map<UUID, OfflinePlayerData> offlinePlayerData = Collections.synchronizedMap(new HashMap<>());
	
	private static final AtomicReference<UUID> currentSelectedFaction = new AtomicReference<>();
	private static final AtomicReference<UUID> clientPlayerUUID = new AtomicReference<>();
	
	
	public static void setCurrentSelectedFaction(UUID factionUUID) {
		currentSelectedFaction.set(factionUUID);
	}
	
	public static void setCurrentSelectedFaction(ClientFactionData factionData) {
		currentSelectedFaction.set(factionData.getFactionUUID());
	}
	
	public static void clearCurrentSelectedFaction() {
		currentSelectedFaction.set(null);
	}
	
	public static Optional<ClientFactionData> getCurrentSelectedFaction() {
		return Optional.ofNullable(currentSelectedFaction.get()).flatMap((uuid) -> {
			synchronized (factionData) {
				return Optional.ofNullable(factionData.get(uuid));
			}
		});
	}
	
	public static Optional<Set<UUID>> getPlayersInCurrentSelectedFaction() {
		return getCurrentSelectedFaction().map(ClientFactionData::getMembers);
	}
	
	public static Optional<String> getPlayerNameIfKnown(UUID playerUUID) {
		return getPlayerData(playerUUID)
		       .map(ClientPlayerData::getPlayerName)
		       .or(() -> getOfflinePlayerData(playerUUID).map(OfflinePlayerData::getPlayerName));
	}
	
	public static Component getPermissionInFaction(UUID playerUUID, UUID FactionUUID) {
		if (playerUUID == null || FactionUUID == null) {
			return Component.translatable("caw.gui.label.empty");
		}
		if (factionData.containsKey(FactionUUID)) {
			Optional<ClientFactionData> factionData = getFactionData(FactionUUID);
			if (factionData.isEmpty()) return Component.translatable("caw.gui.label.empty");
			if (factionData.get().isPlayerOwner(playerUUID)) {
				return Component.translatable("caw.string.faction.owner");
			} else if (factionData.get().isPlayerAdmin(playerUUID)) {
				return Component.translatable("caw.string.faction.admin");
			} else if (factionData.get().isPlayerInFaction(playerUUID)) {
				return Component.translatable("caw.string.faction.member");
			}
		}
		return Component.translatable("caw.gui.label.empty");
	}
	
	public static Component getClientPlayerPermissionInFaction(UUID FactionUUID) {
		if (getClientPlayerUUID().isPresent()) {
			return getPermissionInFaction(clientPlayerUUID.get(), FactionUUID);
		} else {
			return Component.translatable("caw.gui.label.empty");
		}
	}
	
	public static Component getPermissionInCurrentSelectedFaction() {
		if (getCurrentSelectedFaction().isPresent()) {
			return getPermissionInFaction(clientPlayerUUID.get(), getCurrentSelectedFaction().get().getFactionUUID());
		} else {
			return Component.translatable("caw.gui.label.empty");
		}
	}
	
	public static Component getCurrentSelectedFactionName() {
		if (getCurrentSelectedFaction().isPresent()) {
			return Component.literal(getCurrentSelectedFaction().get().getFactionName());
		} else {
			return Component.translatable("caw.gui.label.empty");
		}
	}
	
	public static UUID getCurrentSelectedFactionUUID() {
		synchronized (currentSelectedFaction) {
			return currentSelectedFaction.get();
		}
	}
	
	public synchronized static void clearAllLocalData() {
		clearPlayerData();
		clearFactionData();
		clearInvitations();
		clearOfflinePlayerData();
	}
	
	public static void addPlayerData(ClientPlayerData data) {
		synchronized (playerData) {
			CAWConstants.debugLog("Adding player data: {}", data);
			playerData.put(data.getPlayerUUID(), data);
		}
	}
	
	public static void clearPlayerData() {
		synchronized (playerData) {
			playerData.clear();
		}
	}
	
	public static Set<UUID> getPlayerUUIDs() {
		synchronized (playerData) {
			return Set.copyOf(playerData.keySet());
		}
	}
	
	public static Set<UUID> getPlayerUUIDsExcludeSelf() {
		synchronized (playerData) {
			return playerData.keySet().stream().filter(uuid -> !uuid.equals(clientPlayerUUID.get())).collect(Collectors.toSet());
		}
	}
	
	public static Set<UUID> getUUIDsOfPlayersWhoHasInvitationFromClientPlayer() {
		UUID factionUUID = currentSelectedFaction.get();
		UUID clientPlayerUUID = CAWClientDataManager.clientPlayerUUID.get();
		if (factionUUID == null || clientPlayerUUID == null) return Set.of();
		synchronized (knownInvitations) {
			return knownInvitations.values()
			       .stream()
			       .filter(info -> {
				       if (!info.getFactionUUID().equals(factionUUID)) return false;
				       if (!info.getFromPlayerUUID().equals(clientPlayerUUID)) return false;
				       return info.getToPlayerUUID() != null;
			       })
			       .map(InvitationInfo::getToPlayerUUID)
			       .collect(Collectors.toSet());
		}
	}
	
	public static Optional<ClientPlayerData> getPlayerData(UUID playerUUID) {
		synchronized (playerData) {
			return Optional.ofNullable(playerData.get(playerUUID));
		}
	}
	
	public static Optional<String> getFallbackPlayerNameFromInvitation(UUID toPlayerUUID) {
		synchronized (knownInvitations) {
			return knownInvitations.values().stream()
			       .filter(info -> info.getToPlayerUUID().equals(toPlayerUUID))
			       .map(InvitationInfo::getToPlayerName)
			       .findFirst();
		}
	}
	
	public static void addFactionData(ClientFactionData data) {
		synchronized (factionData) {
			CAWConstants.debugLog("Adding faction data: {}", data);
			factionData.put(data.getFactionUUID(), data);
		}
	}
	
	public static void clearFactionData() {
		synchronized (factionData) {
			factionData.clear();
		}
	}
	
	public static Set<UUID> getFactionUUIDs() {
		synchronized (factionData) {
			return Set.copyOf(factionData.keySet());
		}
	}
	
	public static Optional<ClientFactionData> getFactionData(UUID factionUUID) {
		synchronized (factionData) {
			return Optional.ofNullable(factionData.get(factionUUID));
		}
	}
	
	public static boolean isPlayerInCurrentSelectedFaction(UUID playerUUID) {
		return getCurrentSelectedFaction().map(factionData -> factionData.isPlayerInFaction(playerUUID)).orElse(false);
	}
	
	public static void clearInvitations() {
		synchronized (knownInvitations) {
			knownInvitations.clear();
		}
	}
	
	public static void addInvitation(InvitationInfo info) {
		synchronized (knownInvitations) {
			CAWConstants.debugLog("Adding invitation: {}", info);
			knownInvitations.put(info.getInvitationUUID(), info);
		}
	}
	
	public static void getInvitation(UUID invitationUUID) {
		synchronized (knownInvitations) {
			knownInvitations.get(invitationUUID);
		}
	}
	
	public static Optional<List<InvitationInfo>> findInvitationToSelf() {
		return getClientPlayerUUID().flatMap(CAWClientDataManager::findInvitationTo);
	}
	
	public static Optional<List<InvitationInfo>> findInvitationTo(UUID playerUUID) {
		synchronized (knownInvitations) {
			return Optional.of(knownInvitations.values().stream().filter(info -> {
				if (info.getFromPlayerUUID() == null) return false;
				return info.getToPlayerUUID().equals(playerUUID);
			}).toList()).filter(list -> !list.isEmpty());
		}
	}
	
	public static Optional<List<InvitationInfo>> findInvitationToPlayerForFaction(UUID playerUUID, UUID factionUUID) {
		synchronized (knownInvitations) {
			return Optional.of(knownInvitations.values().stream().filter(info -> {
				if (info.getFromPlayerUUID() == null) return false;
				return info.getToPlayerUUID().equals(playerUUID) && info.getFactionUUID().equals(factionUUID);
			}).toList()).filter(list -> !list.isEmpty());
		}
	}
	
	public static void updateClientPlayerUUID(UUID playerUUID) {
		synchronized (clientPlayerUUID) {
			clientPlayerUUID.set(playerUUID);
		}
	}
	
	public static Optional<UUID> getClientPlayerUUID() {
		synchronized (clientPlayerUUID) {
			return Optional.ofNullable(clientPlayerUUID.get());
		}
	}
	
	public static void addOfflinePlayerData(OfflinePlayerData data) {
		synchronized (offlinePlayerData) {
			CAWConstants.debugLog("Adding offline player data: {}", data);
			offlinePlayerData.put(data.getPlayerUUID(), data);
		}
	}
	
	public static void clearOfflinePlayerData() {
		synchronized (offlinePlayerData) {
			offlinePlayerData.clear();
		}
	}
	
	public static void addDiplomaticRelationship(PTCDiplomaticRelationshipData data) {
		synchronized (diplomaticRelationshipDataOfSelectedFaction) {
			CAWConstants.debugLog("Adding diplomatic relationship data: {}", data);
			diplomaticRelationshipDataOfSelectedFaction.put(data.otherFaction, data.relationship);
		}
	}
	
	public static int getDiplomaticRelationshipWith(UUID factionUUID) {
		synchronized (diplomaticRelationshipDataOfSelectedFaction) {
			return diplomaticRelationshipDataOfSelectedFaction.getOrDefault(factionUUID, DiplomaticRelationshipData.NEUTRAL);
		}
	}
	
	public static void clearDiplomaticRelationshipData() {
		synchronized (diplomaticRelationshipDataOfSelectedFaction) {
			diplomaticRelationshipDataOfSelectedFaction.clear();
		}
	}
	
	public static Optional<OfflinePlayerData> getOfflinePlayerData(UUID playerUUID) {
		synchronized (offlinePlayerData) {
			return Optional.ofNullable(offlinePlayerData.get(playerUUID));
		}
	}
	
	public static boolean isSelfAdminInCurrentFaction() {
		return getCurrentSelectedFaction().map(factionData -> factionData.isPlayerAdmin(clientPlayerUUID.get())).orElse(false);
	}
	
	public static boolean isPlayerAdminInCurrentFaction(UUID playerUUID) {
		return getCurrentSelectedFaction().map(factionData -> factionData.isPlayerAdmin(playerUUID)).orElse(false);
	}
	
	public static boolean isSelfOwnerOfCurrentFaction() {
		return getCurrentSelectedFaction().map(factionData -> factionData.isPlayerOwner(clientPlayerUUID.get())).orElse(false);
	}
	
	public static boolean isLikelyOffline(UUID playerUUID) {
		return !playerData.containsKey(playerUUID);
	}
	
	public static boolean isPlayerOwnerInCurrentFaction(UUID currentPagePlayer) {
		return getCurrentSelectedFaction().map(factionData -> factionData.isPlayerOwner(currentPagePlayer)).orElse(false);
	}
	
	public static boolean isClientPlayerInFaction(UUID factionUUID) {
		synchronized (factionData) {
			return Optional.ofNullable(factionData.get(factionUUID)).map(data -> data.isPlayerInFaction(getClientPlayerUUID().orElse(CAWConstants.NIL_UUID))).orElse(false);
		}
	}
	
	public static int getCurrentSelectedFactionColor() {
		return getCurrentSelectedFaction().map(ClientFactionData::getColor).orElse(16777215);
	}
	
	public static int getCurrentSelectedFactionFakePlayerPolicy() {
		return getCurrentSelectedFaction().map(ClientFactionData::getFakePlayerPolicy).orElse(0);
	}
}
