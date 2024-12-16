package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.misc.OfflinePlayerDatabase;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

public class FactionDataManager extends SavedData {
	
	public static final String DATA_NAME = "caw_factions_data";
	static MinecraftServer SERVER = null;
	
	private static FactionDataManager INSTANCE;
	
	HashMap<UUID, FactionData> factions = new HashMap<>();
	HashMap<UUID, PlayerFactionData> playerFactions = new HashMap<>();
	HashMap<UUID, List<FactionInviteData>> receivedInvitations = new HashMap<>();
	
	private FactionDataManager() {
		this.setDirty();
	}
	
	public static FactionDataManager get() {
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
	
	public void refresh() {
		this.refreshFactionData();
		this.validatePlayerFactions();
		this.updateInvitationPlayerNamesIfKnown();
		this.removeInvalidInvitations();
		this.setDirty();
	}
	
	@SuppressWarnings("RedundantIfStatement")
	private boolean isFactionValid(FactionData faction) {
		if (faction.members.isEmpty()) return false;
		if (faction.members.values().stream().noneMatch(userData -> userData.isOwner)) return false;
		return true;
	}
	
	private void refreshFactionData() {
		this.factions.values().removeIf(faction -> !isFactionValid(faction));
		this.factions.values().forEach(factionData -> factionData.refreshData(SERVER));
	}
	
	Optional<String> getPlayerNameIfOnline(UUID playerUUID) {
		return FactionPacketHandler.getPlayerIfOnline(playerUUID).map(ServerPlayer::getName).map(Component::getString);
	}
	
	Optional<String> getKnownPlayerName(UUID playerUUID) {
		return Optional.ofNullable(this.playerFactions.get(playerUUID))
		       .flatMap(PlayerFactionData::getPrimaryFactionUUID)
		       .map(uuid -> this.factions.get(uuid))
		       .map(factionData -> factionData.getDataOf(playerUUID).getKnownPlayerName());
	}
	
	/**
	 * get the display name of a player, or infer it from known faction data <br>
	 * Depreciated (Inefficient)! Use {@link OfflinePlayerDatabase#getName(UUID) } instead
	 *
	 * @param playerUUID the player UUID
	 * @return the name of the player
	 */
	@Deprecated
	@Contract(pure = true)
	Optional<String> getPlayerName(UUID playerUUID) {
		return getPlayerNameIfOnline(playerUUID).or(() -> getKnownPlayerName(playerUUID));
	}
	
	private void removeInvalidInvitations() {
		this.receivedInvitations.values().forEach(invites -> invites.removeIf(invite -> !this.factions.containsKey(invite.factionUUID)));
		this.receivedInvitations.values().forEach(invites -> invites.removeIf(invite -> !this.isPlayerInFaction(invite.fromPlayerUUID, invite.factionUUID)));
		this.receivedInvitations.values().forEach(invites -> invites.removeIf(invite -> this.isPlayerInFaction(invite.toPlayerUUID, invite.factionUUID)));
		this.setDirty();
	}
	
	private void updateInvitationPlayerNamesIfKnown() {
		for (List<FactionInviteData> invites : this.receivedInvitations.values()) {
			for (FactionInviteData invite : invites) {
				OfflinePlayerDatabase.get().getName(invite.fromPlayerUUID).ifPresent(invite::setFromPlayerName);
				OfflinePlayerDatabase.get().getName(invite.toPlayerUUID).ifPresent(invite::setToPlayerName);
			}
		}
	}
	
	private void validatePlayerFactions() {
		for (FactionData faction : this.factions.values()) {
			for (UUID playerUUID : faction.members.keySet()) {
				PlayerFactionData playerFaction = this.playerFactions.computeIfAbsent(playerUUID, PlayerFactionData::new);
				playerFaction.factions.add(faction.getFactionUUID());
			}
		}
		
		for (Entry<UUID, PlayerFactionData> entry : this.playerFactions.entrySet()) {
			PlayerFactionData playerFaction = entry.getValue();
			playerFaction.playerUUID = entry.getKey();
			playerFaction.factions.removeIf(factionUUID -> {
				if (!this.factions.containsKey(factionUUID)) return true;
				FactionData faction = this.factions.get(factionUUID);
				return !faction.isMember(playerFaction.playerUUID);
			});
			playerFaction.getPrimaryFactionUUID().ifPresentOrElse(factionUUID -> {
				if (!this.factions.containsKey(factionUUID) || !this.factions.get(factionUUID).isMember(playerFaction.playerUUID)) {
					playerFaction.setPrimaryFaction(null);
					tryAssignFallbackPrimary(playerFaction.playerUUID);
				}
			}, () -> {
				tryAssignFallbackPrimary(playerFaction.playerUUID);
			});
		}
	}
	
	@Contract(value = "null -> fail")
	private void tryAssignFallbackPrimary(UUID playerUUID) {
		PlayerFactionData playerFactionData = this.playerFactions.get(playerUUID);
		playerFactionData.getPrimaryFactionUUID().ifPresentOrElse(f -> {
		}, () -> {
			Set<UUID> otherFactions = playerFactionData.getFactions();
			if (!otherFactions.isEmpty()) {
				this.setAsPrimaryFaction(playerUUID, otherFactions.iterator().next());
			}
		});
	}
	
	/**
	 * Gets the faction with the given UUID.
	 *
	 * @param factionUUID The UUID of the faction to get.
	 * @return The faction with the given UUID, if it exists.
	 */
	@Contract(pure = true)
	public Optional<FactionData> getFaction(UUID factionUUID) {
		return Optional.ofNullable(this.factions.get(factionUUID));
	}
	
	/**
	 * Sets the primary faction of the player.
	 *
	 * @param playerUUID  The UUID of the player to set the primary faction of.
	 * @param factionUUID The UUID of the faction to set as primary.
	 */
	@Contract(value = "null,_ -> fail; _,null -> fail")
	public void setAsPrimaryFaction(UUID playerUUID, UUID factionUUID) {
		PlayerFactionData playerFaction = this.playerFactions.computeIfAbsent(playerUUID, PlayerFactionData::new);
		playerFaction.setPrimaryFaction(factionUUID);
	}
	
	/**
	 * Gets the primary faction of the player.
	 *
	 * @param playerUUID The UUID of the player to get the primary faction of.
	 * @return The primary faction of the player, if they have one.
	 */
	@Contract(pure = true, value = "null -> fail")
	public Optional<FactionData> getPrimaryFaction(UUID playerUUID) {
		PlayerFactionData playerFaction = this.playerFactions.computeIfAbsent(playerUUID, PlayerFactionData::new);
		return playerFaction.getPrimaryFactionUUID().map(factionUUID -> this.factions.get(factionUUID)).filter(faction -> faction.isMember(playerUUID));
	}
	
	/**
	 * Checks if the player is in the faction.
	 *
	 * @param playerUUID  The UUID of the player to check.
	 * @param factionUUID The UUID of the faction to check.
	 * @return true if the player is in the faction, false otherwise.
	 */
	@Contract(pure = true, value = "_, null -> fail")
	public boolean isPlayerInFaction(UUID playerUUID, UUID factionUUID) {
		if (!this.factions.containsKey(factionUUID)) return false;
		try {
			return this.factions.get(factionUUID).isMember(playerUUID);
		} catch (Exception e) {
			CAWConstants.LOGGER.error("Error checking if player is in faction", e);
			return false;
		}
	}
	
	/**
	 * Adds the player to the faction.
	 *
	 * @param player      The player to add.
	 * @param factionUUID The faction to add the player to.
	 */
	public void addPlayerToFaction(ServerPlayer player, UUID factionUUID) {
		this.addPlayerToFaction(player, factionUUID, false);
	}
	
	void addPlayerToFaction(ServerPlayer player, UUID factionUUID, @SuppressWarnings("SameParameterValue") boolean ignoreAlreadyIn) {
		if (isPlayerInFaction(player.getUUID(), factionUUID)) {
			if (!ignoreAlreadyIn) {
				return;
			}
		}
		FactionData faction = this.factions.get(factionUUID);
		if (faction == null) return;
		faction.addMember(player.getUUID(), player.getName().getString());
		PlayerFactionData playerFaction = this.playerFactions.computeIfAbsent(player.getUUID(), PlayerFactionData::new);
		playerFaction.factions.add(factionUUID);
		
		this.getPrimaryFaction(player.getUUID()).ifPresentOrElse(f -> {
		}, () -> this.setAsPrimaryFaction(player.getUUID(), factionUUID));
		
		this.refresh();
	}
	
	/**
	 * Checks if the player can be removed from the faction.
	 *
	 * @param playerUUID  The UUID of the player to remove.
	 * @param factionUUID The UUID of the faction to remove the player from.
	 * @return true if the player can be removed, false otherwise.
	 */
	@Contract(pure = true)
	public boolean canRemovePlayerFromFaction(UUID playerUUID, UUID factionUUID) {
		FactionData faction = this.factions.get(factionUUID);
		if (faction == null) return false;
		return faction.canRemoveMember(playerUUID);
	}
	
	/**
	 * Removes the player from the faction.
	 * If {@link #canRemovePlayerFromFaction(UUID, UUID)} returns false, this method does nothing.
	 * <p>
	 * i.e. if the player is admin they should be demoted before being removed.
	 * If the player is owner they should be replaced (or disband the faction).
	 *
	 * @param playerUUID  The UUID of the player to remove.
	 * @param factionUUID The UUID of the faction to remove the player from.
	 * @param isKicked    Whether the player is being kicked or not.
	 */
	public void removePlayerFromFaction(UUID playerUUID, UUID factionUUID, boolean isKicked) {
		this.removePlayerFromFaction(playerUUID, factionUUID, isKicked, false);
	}
	
	public void removePlayerFromFaction(UUID playerUUID, UUID factionUUID, boolean isKicked, boolean force) {
		if (!force) {
			if (!this.canRemovePlayerFromFaction(playerUUID, factionUUID)) return;
		}
		FactionData faction = this.factions.get(factionUUID);
		if (faction == null) return;
		faction.removeMember(playerUUID);
		PlayerFactionData playerFactionData = this.playerFactions.computeIfAbsent(playerUUID, PlayerFactionData::new);
		
		if (isKicked) {
			FactionPacketHandler.getPlayerIfOnline(playerUUID).ifPresent(player -> {
				player.sendSystemMessage(Component.translatable("caw.message.faction.kicked", faction.getFactionName()));
			});
		}
		playerFactionData.factions.remove(factionUUID);
		if (playerFactionData.getPrimaryFactionUUID().map(factionUUID::equals).orElse(false)) {
			playerFactionData.setPrimaryFaction(null);
		}
		this.tryAssignFallbackPrimary(playerUUID);
		this.refresh();
	}
	
	public void disbandFaction(UUID factionUUID) {
		FactionData faction = this.factions.get(factionUUID);
		if (faction == null) return;
		List<UUID> toRemove = new ArrayList<>(faction.members.keySet());
		toRemove.forEach(playerUUID -> this.removePlayerFromFaction(playerUUID, factionUUID, false, true));
		this.factions.remove(factionUUID);
		FactionClaimDataManager.get().refresh(true);
		this.refresh();
	}
	
	/**
	 * Sets the diplomatic relationship from one faction to another. <br>
	 * Note: relationship is directional, so if faction A allies to faction B, it does not mean faction B allies to faction A.
	 *
	 * @param fromFactionUUID the faction that is setting the relationship
	 * @param toFactionUUID   the faction that the relationship is set to
	 * @param relationship    the relationship to set
	 */
	public void setDiplomaticRelationship(UUID fromFactionUUID, UUID toFactionUUID, int relationship) {
		FactionData fromFaction = this.factions.get(fromFactionUUID);
		FactionData toFaction = this.factions.get(toFactionUUID);
		if (fromFaction == null || toFaction == null) return;
		fromFaction.setDiplomaticRelationship(toFactionUUID, relationship);
		this.refresh();
	}
	
	/**
	 * Gets the diplomatic relationship from one faction to another.<br>
	 * Note that relationship is directional, so e.g. <br>
	 * if you want to check can player in faction A do something to faction B's block (i.e. A's permission on B's side)<br>
	 * you need to check relationship from B to A.
	 *
	 * @param fromFactionUUID the faction that is checking the relationship
	 * @param toFactionUUID   the faction that the relationship is checked to
	 * @return the relationship from fromFaction to toFaction
	 */
	public int getDiplomaticRelationship(UUID fromFactionUUID, UUID toFactionUUID) {
		FactionData fromFaction = this.factions.get(fromFactionUUID);
		if (fromFaction == null) return 0;
		return fromFaction.getDiplomaticRelationship(toFactionUUID);
	}
	
	/**
	 * Gets factions that the player is an owner of.
	 *
	 * @param playerUUID The UUID of the player to get the factions of.
	 * @return A list of factions that the player is an owner of.
	 */
	@Contract(pure = true)
	public List<FactionData> getOwnedFactions(UUID playerUUID) {
		return this.factions.values().stream().filter(faction -> faction.isOwner(playerUUID)).toList();
	}
	
	/**
	 * Checks if the player is an admin or owner of the faction.
	 *
	 * @param playerUUID  The UUID of the player to check.
	 * @param factionUUID The UUID of the faction to check.
	 * @return true if the player is an admin or owner of the faction, false otherwise.
	 */
	@Contract(pure = true)
	public boolean isPlayerAdminOrOwnerOfFaction(UUID playerUUID, UUID factionUUID) {
		return this.factions.get(factionUUID).isAdminOrOwner(playerUUID);
	}
	
	/**
	 * Checks if the player is the owner of the faction.
	 *
	 * @param playerUUID  The UUID of the player to check.
	 * @param factionUUID The UUID of the faction to check.
	 * @return true if the player is the owner of the faction, false otherwise.
	 */
	@Contract(pure = true)
	public boolean isOwnerOfFaction(UUID playerUUID, UUID factionUUID) {
		return this.factions.get(factionUUID).isOwner(playerUUID);
	}
	
	public boolean isAdminOfFaction(UUID playerUUID, UUID factionUUID) {
		return this.factions.get(factionUUID).isAdmin(playerUUID);
	}
	
	/**
	 * Sets the new owner of the faction.
	 *
	 * @param playerUUID  The UUID of the player to set as owner.
	 * @param factionUUID The UUID of the faction to set the owner of.
	 */
	public void setNewOwner(UUID playerUUID, UUID factionUUID) {
		this.factions.get(factionUUID).setOwner(playerUUID);
	}
	
	/**
	 * Check if one player's permission in a faction is higher than or same as the other (owner > admin > member)
	 *
	 * @param thisPlayer    player 1
	 * @param toCheckPlayer player 2
	 * @param factionUUID   the faction to get permission level from
	 * @return true if permission of toCheckPlayer >= permission of thisPlayer, false otherwise. <br>
	 * If thisPlayer and toCheckPlayer are the same individual, return true no matter what. <br>
	 * If thisPlayer is not in the faction, always return true.
	 */
	@Contract(pure = true)
	public boolean isPermissionEqualOrHigher(UUID thisPlayer, UUID toCheckPlayer, UUID factionUUID) {
		if (thisPlayer.equals(toCheckPlayer)) return true;
		
		if (isOwnerOfFaction(thisPlayer, factionUUID)) {
			return isOwnerOfFaction(toCheckPlayer, factionUUID);
		}
		if (isAdminOfFaction(thisPlayer, factionUUID)) {
			return isPlayerAdminOrOwnerOfFaction(toCheckPlayer, factionUUID);
		}
		if (isPlayerInFaction(thisPlayer, factionUUID)) {
			return isPlayerInFaction(toCheckPlayer, factionUUID);
		}
		return true;
	}
	
	/**
	 * Check if one player's permission in a faction is strictly higher than the other (owner > admin > member)
	 *
	 * @param thisPlayer    player 1
	 * @param toCheckPlayer player 2
	 * @param factionUUID   the faction to get permission level from
	 * @return true if permission of toCheckPlayer > permission of thisPlayer, false otherwise. <br>
	 * If thisPlayer and toCheckPlayer are the same individual, return true no matter what. <br>
	 * If thisPlayer is not in the faction, and toCheckPlayer is in the faction, then returns true (member > stranger). False otherwise.
	 */
	@Contract(pure = true)
	public boolean isPermissionStrictlyHigher(UUID thisPlayer, UUID toCheckPlayer, UUID factionUUID) {
		if (thisPlayer.equals(toCheckPlayer)) return true;
		
		if (isOwnerOfFaction(thisPlayer, factionUUID)) {
			return false;
		}
		if (isAdminOfFaction(thisPlayer, factionUUID)) {
			return isOwnerOfFaction(toCheckPlayer, factionUUID);
		}
		if (isPlayerInFaction(thisPlayer, factionUUID)) {
			return isPlayerAdminOrOwnerOfFaction(toCheckPlayer, factionUUID);
		}
		return isPlayerInFaction(toCheckPlayer, factionUUID);
	}
	
	/**
	 * get all factions
	 *
	 * @return all factions
	 */
	@Contract(pure = true)
	public List<FactionData> getAllFactions() {
		return INSTANCE.factions.values().stream().toList();
	}
	
	/**
	 * get factions the given player is in
	 *
	 * @param playerUUID the player to get factions for
	 * @return all factions
	 */
	@Contract(pure = true)
	public List<FactionData> getFactionsForPlayer(UUID playerUUID) {
		return this.playerFactions.computeIfAbsent(playerUUID, PlayerFactionData::new).factions.stream().map(this.factions::get).toList();
	}
	
	public void sendMessageToAllPlayersOnlineInFaction(UUID factionUUID, Component message) {
		SERVER.getPlayerList().getPlayers().stream().filter(player -> INSTANCE.isPlayerInFaction(player.getUUID(), factionUUID)).forEach(player -> player.sendSystemMessage(message));
	}
	
	/**
	 * get invitations sent to the player
	 *
	 * @param playerUUID the player to get invitations for
	 * @return all invitations sent to the player
	 */
	@Contract(pure = true)
	public List<FactionInviteData> getInvitationsToPlayer(UUID playerUUID) {
		return this.receivedInvitations.getOrDefault(playerUUID, new ArrayList<>());
	}
	
	/**
	 * get invitations sent from the player
	 *
	 * @param playerUUID the player to get invitations from
	 * @return all invitations sent by the player
	 */
	@Contract(pure = true)
	public List<FactionInviteData> getInvitationsFromPlayer(UUID playerUUID) {
		return this.receivedInvitations.values().stream().flatMap(Collection::stream).filter(invite -> invite.fromPlayerUUID.equals(playerUUID)).toList();
	}
	
	/**
	 * get invitations sent to a given player, that invite them to a given faction
	 *
	 * @param playerUUID  the player to get invitations for
	 * @param factionUUID the faction to get invitations for
	 * @return all invitations matching the criteria
	 */
	@Contract(pure = true)
	public List<FactionInviteData> getInvitationsToPlayerForFaction(UUID playerUUID, UUID factionUUID) {
		return this.receivedInvitations.getOrDefault(playerUUID, new ArrayList<>()).stream().filter(invite -> invite.factionUUID.equals(factionUUID)).toList();
	}
	
	/**
	 * get invitations sent from a given player to a given player
	 *
	 * @param fromPlayerUUID the player who sent the invitations
	 * @param toPlayerUUID   the player that invitations were sent to
	 * @return all invitations matching the criteria
	 */
	@Contract(pure = true)
	public List<FactionInviteData> getInvitationsFromPlayerToPlayer(UUID fromPlayerUUID, UUID toPlayerUUID) {
		return this.receivedInvitations.getOrDefault(toPlayerUUID, new ArrayList<>()).stream().filter(invite -> invite.fromPlayerUUID.equals(fromPlayerUUID)).toList();
	}
	
	/**
	 * get invitations sent from a given player to a given player, that invite them to a given faction
	 *
	 * @param fromPlayerUUID the player who sent the invitations
	 * @param toPlayerUUID   the player that invitations were sent to
	 * @param factionUUID    the faction to get invitations for
	 * @return all invitations matching the criteria
	 */
	@Contract(pure = true)
	public List<FactionInviteData> getInvitationsFromPlayerToPlayerForFaction(UUID fromPlayerUUID, UUID toPlayerUUID, UUID factionUUID) {
		return this.receivedInvitations.getOrDefault(toPlayerUUID, new ArrayList<>()).stream().filter(invite -> invite.fromPlayerUUID.equals(fromPlayerUUID) && invite.factionUUID.equals(factionUUID)).toList();
	}
	
	/**
	 * add an invitation to the player
	 *
	 * @param invite the invitation to add
	 */
	@Contract(pure = true)
	public void addInvitation(FactionInviteData invite) {
		this.receivedInvitations.computeIfAbsent(invite.toPlayerUUID, uuid -> new ArrayList<>()).add(invite);
		this.setDirty();
	}
	
	@Contract(pure = true)
	public void removeInvitation(FactionInviteData invite) {
		this.receivedInvitations.computeIfAbsent(invite.toPlayerUUID, uuid -> new ArrayList<>()).removeIf(invite1 -> invite1.invitationUUID.equals(invite.invitationUUID));
		this.setDirty();
	}
	
	@Contract(pure = true)
	public void removeInvitation(UUID invitationUUID) {
		this.receivedInvitations.values().forEach(invites -> invites.removeIf(invite -> invite.invitationUUID.equals(invitationUUID)));
		this.setDirty();
	}
	
	@Contract(pure = true)
	public Optional<FactionInviteData> getInvitation(UUID invitationUUID) {
		return this.receivedInvitations.values().stream().flatMap(Collection::stream).filter(invite -> invite.invitationUUID.equals(invitationUUID)).findFirst();
	}
	
	private static FactionDataManager loadOrCreate(MinecraftServer server) {
		if (server == null) return new FactionDataManager();
		ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
		assert overworld != null;
		CAWConstants.LOGGER.info("Loading factions data");
		return overworld.getDataStorage().computeIfAbsent(FactionDataManager::load, FactionDataManager::new, DATA_NAME);
	}
	
	public static FactionDataManager load(CompoundTag tag) {
		FactionDataManager data = new FactionDataManager();
		
		ListTag factionList = tag.getList("factions", ListTag.TAG_COMPOUND);
		factionList.forEach(tag1 -> {
			CompoundTag factionTag = (CompoundTag) tag1;
			FactionData faction = new FactionData().readFromNBT(factionTag);
			faction.refreshData(SERVER);
			data.factions.put(faction.getFactionUUID(), faction);
		});
		
		ListTag playerFactionList = tag.getList("playerFactions", ListTag.TAG_COMPOUND);
		playerFactionList.forEach(tag1 -> {
			CompoundTag playerFactionTag = (CompoundTag) tag1;
			PlayerFactionData playerFaction = new PlayerFactionData().readFromNBT(playerFactionTag);
			data.playerFactions.put(playerFaction.playerUUID, playerFaction);
		});
		data.validatePlayerFactions();
		
		ListTag receivedInvitationsList = tag.getList("receivedInvitations", ListTag.TAG_COMPOUND);
		receivedInvitationsList.forEach(tag1 -> {
			CompoundTag playerInvitesTag = (CompoundTag) tag1;
			UUID playerUUID = UUID.fromString(playerInvitesTag.getString("playerUUID"));
			List<FactionInviteData> invites = new ArrayList<>();
			ListTag invitesList = playerInvitesTag.getList("invites", ListTag.TAG_COMPOUND);
			invitesList.forEach(tag2 -> {
				CompoundTag inviteTag = (CompoundTag) tag2;
				FactionInviteData invite = new FactionInviteData().readFromNBT(inviteTag);
				invites.add(invite);
			});
			data.receivedInvitations.put(playerUUID, invites);
		});
		
		data.refresh();
		return data;
	}
	
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		ListTag factionList = new ListTag();
		this.factions.forEach((uuid, factionData) -> {
			CompoundTag factionTag = factionData.writeToNBT(new CompoundTag());
			factionList.add(factionTag);
		});
		tag.put("factions", factionList);
		
		ListTag playerFactionList = new ListTag();
		this.playerFactions.forEach((uuid, playerFactionData) -> {
			CompoundTag playerFactionTag = playerFactionData.writeToNBT(new CompoundTag());
			playerFactionList.add(playerFactionTag);
		});
		tag.put("playerFactions", playerFactionList);
		
		ListTag receivedInvitationsList = new ListTag();
		this.receivedInvitations.forEach((uuid, invites) -> {
			CompoundTag playerInvitesTag = new CompoundTag();
			playerInvitesTag.putString("playerUUID", uuid.toString());
			ListTag invitesList = new ListTag();
			invites.forEach(invite -> {
				CompoundTag inviteTag = invite.writeToNBT(new CompoundTag());
				invitesList.add(inviteTag);
			});
			playerInvitesTag.put("invites", invitesList);
			receivedInvitationsList.add(playerInvitesTag);
		});
		tag.put("receivedInvitations", receivedInvitationsList);
		
		return tag;
	}
	
	public void setFakePlayerPolicy(UUID faction, int policy) {
		this.factions.get(faction).setFakePlayerPolicy(policy);
		this.refresh();
	}
	
	public String getFactionName(UUID factionUUID) {
		return this.factions.get(factionUUID).getFactionName();
	}
}
