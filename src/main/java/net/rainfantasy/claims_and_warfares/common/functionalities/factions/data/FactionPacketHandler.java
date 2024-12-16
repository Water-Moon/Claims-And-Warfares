package net.rainfantasy.claims_and_warfares.common.functionalities.factions.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionInfo;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.InvitationInfo;
import net.rainfantasy.claims_and_warfares.common.functionalities.misc.OfflinePlayerDatabase;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.SerializableDateTime;
import net.rainfantasy.claims_and_warfares.common.utils.PlayerThrottleUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("DuplicatedCode")
public class FactionPacketHandler {
	
	private static final int PLAYER_MAX_OWNED_FACTIONS = 3;
	private static final int FACTION_NAME_MIN_LENGTH = 2;
	private static final int FACTION_NAME_MAX_LENGTH = 16;
	private static final char[] FACTION_NAME_DISALLOWED_CHARS = new char[]{':', '\\', '/', '-', '#', '(', ')'};
	private static final int PLAYER_MAX_OUTGOING_INVITATIONS = 10;
	
	/**
	 * Checks for any error that prevents the creation of a faction.
	 *
	 * @param playerUUID  The UUID of the player creating the faction.
	 * @param factionName The name of the faction to create.
	 * @return An error message if something went wrong, empty otherwise.
	 */
	private static Optional<Component> checkFactionCreationError(UUID playerUUID, String factionName) {
		if (PlayerThrottleUtil.throttlePlayerOperation(playerUUID)) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		//check for empty name
		if (factionName.isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.name_empty"));
		}
		
		//check for same name
		if (FactionDataManager.get().factions.values().stream().anyMatch(f -> f.factionName.equals(factionName))) {
			return Optional.of(Component.translatable("caw.errors.faction.name_conflict"));
		}
		
		if (factionName.length() < FACTION_NAME_MIN_LENGTH || factionName.length() > FACTION_NAME_MAX_LENGTH) {
			return Optional.of(Component.translatable("caw.errors.faction.name_length", FACTION_NAME_MIN_LENGTH, FACTION_NAME_MAX_LENGTH));
		}
		
		if (factionName.chars().anyMatch(c -> {
			for (char disallowed : FACTION_NAME_DISALLOWED_CHARS) {
				if (c == disallowed) {
					return true;
				}
			}
			return false;
		})) {
			return Optional.of(Component.translatable("caw.errors.faction.name_disallowed_chars"));
		}
		
		char firstChar = factionName.charAt(0);
		int charType = Character.getType(firstChar);
		if (charType == Character.DASH_PUNCTUATION || charType == Character.START_PUNCTUATION || charType == Character.END_PUNCTUATION || charType == Character.CONNECTOR_PUNCTUATION || charType == Character.OTHER_PUNCTUATION) {
			return Optional.of(Component.translatable("caw.errors.faction.name_must_start_with_alphanumeric"));
		}
		
		//check for too many factions
		if (FactionDataManager.get().getOwnedFactions(playerUUID).size() >= PLAYER_MAX_OWNED_FACTIONS) {
			return Optional.of(Component.translatable("caw.errors.faction.owned_too_many", PLAYER_MAX_OWNED_FACTIONS));
		}
		
		return Optional.empty();
	}
	
	/**
	 * Creates a new faction with the given name and the player as owner.
	 *
	 * @param player      The player creating the faction.
	 * @param factionName The name of the faction to create.
	 * @return The created faction or an error message if something went wrong (e.g. owned too many factions or name already taken).
	 */
	public static Either<FactionData, Component> createFaction(ServerPlayer player, String factionName) {
		UUID playerUUID = player.getUUID();
		Optional<Component> error = checkFactionCreationError(playerUUID, factionName);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionData faction = new FactionData(UUID.randomUUID(), factionName, player);
		FactionDataManager.get().factions.put(faction.getFactionUUID(), faction);
		FactionDataManager.get().refresh();
		FactionDataManager.get().addPlayerToFaction(player, faction.getFactionUUID());
		return Either.left(faction);
	}
	
	public static Optional<Component> checkFactionRenameError(ServerPlayer player, UUID factionUUID, String factionName) {
		if (PlayerThrottleUtil.throttlePlayerOperation(factionUUID)) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		if (!FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_admin"));
		}
		if (factionName.isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.name_empty"));
		}
		if (factionName.equals(FactionDataManager.get().factions.get(factionUUID).factionName)) {
			return Optional.of(Component.translatable("caw.errors.faction.name_same"));
		}
		if (FactionDataManager.get().factions.values().stream().anyMatch(f -> f.factionName.equals(factionName))) {
			return Optional.of(Component.translatable("caw.errors.faction.name_conflict"));
		}
		if (factionName.length() < FACTION_NAME_MIN_LENGTH || factionName.length() > FACTION_NAME_MAX_LENGTH) {
			return Optional.of(Component.translatable("caw.errors.faction.name_length", FACTION_NAME_MIN_LENGTH, FACTION_NAME_MAX_LENGTH));
		}
		if (factionName.chars().anyMatch(c -> {
			for (char disallowed : FACTION_NAME_DISALLOWED_CHARS) {
				if (c == disallowed) {
					return true;
				}
			}
			return false;
		})) {
			return Optional.of(Component.translatable("caw.errors.faction.name_disallowed_chars"));
		}
		char firstChar = factionName.charAt(0);
		int charType = Character.getType(firstChar);
		if (charType == Character.DASH_PUNCTUATION || charType == Character.START_PUNCTUATION || charType == Character.END_PUNCTUATION || charType == Character.CONNECTOR_PUNCTUATION || charType == Character.OTHER_PUNCTUATION) {
			return Optional.of(Component.translatable("caw.errors.faction.name_must_start_with_alphanumeric"));
		}
		return Optional.empty();
	}
	
	public static Either<Pair<String, String>, Component> renameFaction(ServerPlayer player, UUID factionUUID, String factionName) {
		Optional<Component> error = checkFactionRenameError(player, factionUUID, factionName);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		String oldName = FactionDataManager.get().factions.get(factionUUID).factionName;
		FactionDataManager.get().factions.get(factionUUID).factionName = factionName;
		FactionDataManager.get().refresh();
		sendUpdateToOnlineMembers(factionUUID);
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(factionUUID,
		Component.translatable("caw.message.faction.faction_renamed", oldName, factionName));
		return Either.left(Pair.of(oldName, factionName));
	}
	
	public static Optional<Component> checkSetColorError(ServerPlayer player, UUID factionUUID, int color) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		if (!FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_admin"));
		}
		return Optional.empty();
	}
	
	public static Either<Integer, Component> setColor(ServerPlayer player, UUID factionUUID, int color) {
		Optional<Component> error = checkSetColorError(player, factionUUID, color);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionDataManager.get().factions.get(factionUUID).setFactionColor(color);
		FactionDataManager.get().refresh();
		sendUpdateToOnlineMembers(factionUUID);
		return Either.left(color);
	}
	
	public static Optional<Component> checkSetPrimaryFactionError(ServerPlayer player, UUID factionUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		if (FactionDataManager.get().getPrimaryFaction(player.getUUID()).map(f -> f.getFactionUUID().equals(factionUUID)).orElse(false)) {
			return Optional.of(Component.translatable("caw.errors.faction.already_primary"));
		}
		return Optional.empty();
	}
	
	/**
	 * Sets the primary faction of the player to the given faction.
	 *
	 * @param player      The player to set the primary faction for.
	 * @param factionUUID The UUID of the faction to set as primary.
	 * @return The faction data of the new primary faction or an error message if something went wrong.
	 */
	public static Either<FactionData, Component> setPrimaryFaction(ServerPlayer player, UUID factionUUID) {
		Optional<Component> error = checkSetPrimaryFactionError(player, factionUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionDataManager.get().setAsPrimaryFaction(player.getUUID(), factionUUID);
		return Either.left(FactionDataManager.get().factions.get(factionUUID));
	}
	
	public static Optional<Component> checkCanInvite(ServerPlayer from, UUID to, UUID toFaction) {
		if (PlayerThrottleUtil.throttlePlayerOperation(from.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		//faction must exist
		if (FactionDataManager.get().getFaction(toFaction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		
		//source player must be in that faction to send an invitation
		if (!FactionDataManager.get().isPlayerInFaction(from.getUUID(), toFaction)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		
		//target player must not be in that faction
		if (FactionDataManager.get().isPlayerInFaction(to, toFaction)) {
			return Optional.of(Component.translatable("caw.errors.faction.already_in_faction.other"));
		}
		
		//source player should have not sent the same invite before
		if (!FactionDataManager.get().getInvitationsFromPlayerToPlayerForFaction(from.getUUID(), to, toFaction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.invite.already_sent"));
		}
		
		//the player must not have too many outgoing invitations
		if (FactionDataManager.get().getInvitationsFromPlayer(from.getUUID()).size() >= PLAYER_MAX_OUTGOING_INVITATIONS) {
			return Optional.of(Component.translatable("caw.errors.faction.invite.too_many_outgoing"));
		}
		
		return Optional.empty();
	}
	
	/**
	 * Invites a player to a faction.
	 *
	 * @param from      The player sending the invitation.
	 * @param to        The player to invite.
	 * @param toFaction The faction to invite the player to.
	 * @return The invitation data or an error message if something went wrong.
	 */
	public static Either<InvitationInfo, Component> invitePlayer(ServerPlayer from, UUID to, UUID toFaction) {
		Optional<Component> error = checkCanInvite(from, to, toFaction);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		if (from.getServer() == null) {
			CAWConstants.LOGGER.error("Server is null for player {} (probably Minecraft bug)", from.getUUID());
			CAWConstants.LOGGER.error("Stacktrace:", new NullPointerException());
			return Either.right(Component.translatable("caw.errors.generic.not_your_fault"));
		}
		UUID fromPlayerUUID = from.getUUID();
		String fromPlayerName = from.getGameProfile().getName();
		
		String toPlayerName;
		Optional<ServerPlayer> player = getPlayerIfOnline(to);
		if (player.isEmpty()) {
			toPlayerName = "Unknown[" + to.toString().substring(0, 8) + "]";
		} else {
			toPlayerName = player.get().getGameProfile().getName();
		}
		
		SerializableDateTime sentTime = new SerializableDateTime();
		
		FactionInviteData data = new FactionInviteData(fromPlayerUUID, fromPlayerName, to, toPlayerName, toFaction, sentTime);
		FactionDataManager.get().addInvitation(
		data
		);
		sendUpdateIfOnline(to);
		player.ifPresent(p -> {
			p.sendSystemMessage(Component.translatable("caw.message.faction.invite_received", fromPlayerName, FactionDataManager.get().factions.get(toFaction).factionName));
		});
		
		return Either.left(new InvitationInfo(data));
	}
	
	public static Optional<Component> checkWithdrawInvite(ServerPlayer from, UUID to, UUID toFaction) {
		if (PlayerThrottleUtil.throttlePlayerOperation(from.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		//faction must exist
		if (FactionDataManager.get().getFaction(toFaction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		//source player must be in that faction to withdraw an invitation
		if (!FactionDataManager.get().isPlayerInFaction(from.getUUID(), toFaction)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		//the player must have sent the invite before
		if (FactionDataManager.get().getInvitationsFromPlayerToPlayerForFaction(from.getUUID(), to, toFaction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.invite_withdrawal.not_sent"));
		}
		return Optional.empty();
	}
	
	/**
	 * Withdraws an invitation to a player.
	 *
	 * @param from      The player withdrawing the invitation.
	 * @param to        The player to withdraw the invitation to.
	 * @param toFaction The faction the invitation was for.
	 * @return The invitation data or an error message if something went wrong.
	 */
	public static Either<InvitationInfo, Component> withdrawInvite(ServerPlayer from, UUID to, UUID toFaction) {
		Optional<Component> error = checkWithdrawInvite(from, to, toFaction);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		AtomicReference<FactionInviteData> dataRef = new AtomicReference<>();
		FactionDataManager.get().getInvitationsFromPlayerToPlayerForFaction(from.getUUID(), to, toFaction)
		.stream()
		.findFirst()
		.ifPresent(data -> {
			dataRef.set(data);
			FactionDataManager.get().removeInvitation(data);
			sendUpdateIfOnline(to);
			getPlayerIfOnline(to).ifPresent(p -> {
				p.sendSystemMessage(Component.translatable("caw.message.faction.invite_withdrawn", from.getGameProfile().getName(), FactionDataManager.get().factions.get(toFaction).factionName));
			});
		});
		if (dataRef.get() == null) {
			CAWConstants.LOGGER.error("Failed to find and remove invitation for player {} to {} in faction {} (probably Minecraft bug)", from.getUUID(), to, toFaction);
			CAWConstants.LOGGER.error("Stacktrace:", new NullPointerException());
			return Either.right(Component.translatable("caw.errors.generic.not_your_fault"));
		}
		return Either.left(new InvitationInfo(dataRef.get()));
	}
	
	public static Optional<Component> checkAcceptInvite(ServerPlayer player, UUID invitationUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the invitation must still exist
		if (FactionDataManager.get().getInvitation(invitationUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.invite.not_found"));
		}
		return Optional.empty();
	}
	
	public static Optional<Component> checkDeclineInvite(ServerPlayer player, UUID invitationUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the invitation must still exist
		if (FactionDataManager.get().getInvitation(invitationUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.invite.not_found"));
		}
		return Optional.empty();
	}
	
	/**
	 * Accepts an invitation to a faction.
	 *
	 * @param player         The player accepting the invitation.
	 * @param invitationUUID The UUID of the invitation to accept.
	 * @return The faction info of the faction the player joined or an error message if something went wrong.
	 */
	public static Either<FactionInfo, Component> acceptInvite(ServerPlayer player, UUID invitationUUID) {
		Optional<Component> error = checkAcceptInvite(player, invitationUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionInviteData data = FactionDataManager.get().getInvitation(invitationUUID).orElseThrow(() -> new RuntimeException("Invitation not found but how?"));
		FactionDataManager.get().removeInvitation(data);
		FactionDataManager.get().addPlayerToFaction(player, data.factionUUID);
		getPlayerIfOnline(data.fromPlayerUUID).ifPresent(p -> {
			p.sendSystemMessage(Component.translatable("caw.message.faction.invite_accepted",
			player.getGameProfile().getName(),
			FactionDataManager.get().factions.get(data.factionUUID).getFactionName()
			));
		});
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(
		data.factionUUID,
		Component.translatable("caw.message.faction.player_joined.from_invite",
		player.getGameProfile().getName(),
		data.fromPlayerName,
		FactionDataManager.get().factions.get(data.factionUUID).getFactionName()
		)
		);
		sendUpdateToOnlineMembers(data.factionUUID);
		return Either.left(new FactionInfo(FactionDataManager.get().factions.get(data.factionUUID)));
	}
	
	/**
	 * Declines an invitation to a faction.
	 *
	 * @param player         The player declining the invitation.
	 * @param invitationUUID The UUID of the invitation to decline.
	 * @return The invitation info of the declined invitation or an error message if something went wrong.
	 */
	public static Either<InvitationInfo, Component> declineInvite(ServerPlayer player, UUID invitationUUID) {
		Optional<Component> error = checkDeclineInvite(player, invitationUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionInviteData data = FactionDataManager.get().getInvitation(invitationUUID).orElseThrow(() -> new RuntimeException("Invitation not found but how?"));
		FactionDataManager.get().removeInvitation(data);
		sendUpdateIfOnline(data.fromPlayerUUID);
		getPlayerIfOnline(data.fromPlayerUUID).ifPresent(p -> {
			p.sendSystemMessage(Component.translatable("caw.message.faction.invite_declined",
			player.getGameProfile().getName(),
			FactionDataManager.get().factions.get(data.factionUUID).getFactionName()
			));
		});
		return Either.left(new InvitationInfo(data));
	}
	
	public static Optional<Component> checkLeaveFaction(ServerPlayer player, UUID factionUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the faction must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// the player must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// the player must not be the owner of the faction
		if (FactionDataManager.get().isOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.owner_cannot_leave"));
		}
		return Optional.empty();
	}
	
	/**
	 * Makes a player leave a faction.
	 *
	 * @param player      The player to make leave the faction.
	 * @param factionUUID The UUID of the faction to leave.
	 * @return The faction info of the faction the player left or an error message if something went wrong.
	 */
	public static Either<FactionInfo, Component> leaveFaction(ServerPlayer player, UUID factionUUID) {
		Optional<Component> error = checkLeaveFaction(player, factionUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		//forced because one should be able to leave no matter what (unless owner)
		FactionDataManager.get().removePlayerFromFaction(player.getUUID(), factionUUID, false, true);
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(
		factionUUID,
		Component.translatable("caw.message.faction.player_left",
		player.getGameProfile().getName(),
		FactionDataManager.get().factions.get(factionUUID).getFactionName()
		)
		);
		sendUpdateToOnlineMembers(factionUUID);
		return Either.left(new FactionInfo(FactionDataManager.get().factions.get(factionUUID)));
	}
	
	public static Optional<Component> tryMakeAdmin(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be owner
		if (!FactionDataManager.get().isOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_owner"));
		}
		// that member must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(targetPlayerUUID, factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction.other"));
		}
		// that member must not be admin already
		if (FactionDataManager.get().getFaction(factionUUID).map(result -> result.isAdmin(targetPlayerUUID)).orElse(false)) {
			return Optional.of(Component.translatable("caw.errors.faction.already_admin"));
		}
		return Optional.empty();
	}
	
	/**
	 * Makes a player an admin of a faction.
	 *
	 * @param player           The player making the other player an admin.
	 * @param factionUUID      The UUID of the faction to make the player an admin of.
	 * @param targetPlayerUUID The UUID of the player to make an admin.
	 * @return The faction member data of the player that was made an admin or an error message if something went wrong.
	 */
	public static Either<FactionMemberData, Component> makeAdmin(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		Optional<Component> error = tryMakeAdmin(player, factionUUID, targetPlayerUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionDataManager.get().getFaction(factionUUID).orElseThrow().addAdmin(targetPlayerUUID);
		getPlayerIfOnline(targetPlayerUUID).ifPresent(p -> {
			p.sendSystemMessage(Component.translatable("caw.message.faction.now_admin",
			player.getGameProfile().getName(),
			FactionDataManager.get().factions.get(factionUUID).getFactionName()
			));
		});
		sendUpdateToOnlineMembers(factionUUID);
		return Either.left(FactionDataManager.get().getFaction(factionUUID).orElseThrow().members.get(targetPlayerUUID));
	}
	
	public static Optional<Component> tryRemoveAdmin(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be owner
		if (!FactionDataManager.get().isOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_owner"));
		}
		// that member must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(targetPlayerUUID, factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction.other"));
		}
		// that member must be admin
		if (!FactionDataManager.get().getFaction(factionUUID).map(entry -> entry.isAdmin(targetPlayerUUID)).orElse(false)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_admin"));
		}
		return Optional.empty();
	}
	
	/**
	 * Removes a player's admin status in a faction.
	 *
	 * @param player           The player removing the other player's admin status.
	 * @param factionUUID      The UUID of the faction to remove the player's admin status from.
	 * @param targetPlayerUUID The UUID of the player to remove the admin status from.
	 * @return The faction member data of the player that was removed from admin or an error message if something went wrong.
	 */
	public static Either<FactionMemberData, Component> removeAdmin(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		Optional<Component> error = tryRemoveAdmin(player, factionUUID, targetPlayerUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionDataManager.get().getFaction(factionUUID).orElseThrow().removeAdmin(targetPlayerUUID);
		getPlayerIfOnline(targetPlayerUUID).ifPresent(p -> {
			p.sendSystemMessage(Component.translatable("caw.message.faction.no_longer_admin",
			player.getGameProfile().getName(),
			FactionDataManager.get().factions.get(factionUUID).getFactionName()
			));
		});
		sendUpdateToOnlineMembers(factionUUID);
		return Either.left(FactionDataManager.get().getFaction(factionUUID).orElseThrow().members.get(targetPlayerUUID));
	}
	
	public static Optional<Component> tryAssignNewOwner(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be owner
		if (!FactionDataManager.get().isOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_owner"));
		}
		// that member must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(targetPlayerUUID, factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction.other"));
		}
		return Optional.empty();
	}
	
	/**
	 * Assigns a new owner to a faction.
	 *
	 * @param player           The player assigning the new owner.
	 * @param factionUUID      The UUID of the faction to assign the new owner to.
	 * @param targetPlayerUUID The UUID of the player to assign as the new owner.
	 * @return The faction member data of the new owner or an error message if something went wrong.
	 */
	public static Either<Tuple<FactionData, FactionMemberData>, Component> assignNewOwner(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		Optional<Component> error = tryAssignNewOwner(player, factionUUID, targetPlayerUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		FactionDataManager.get().getFaction(factionUUID).orElseThrow().setOwner(targetPlayerUUID);
		sendUpdateToOnlineMembers(factionUUID);
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(factionUUID, Component.translatable("caw.message.faction.transfer_message",
		player.getGameProfile().getName(),
		FactionDataManager.get().factions.get(factionUUID).getFactionName(),
		OfflinePlayerDatabase.get().getName(targetPlayerUUID).orElse("Unknown[" + targetPlayerUUID.toString().substring(0, 8) + "]")
		));
		getPlayerIfOnline(targetPlayerUUID).ifPresent(targetPlayer -> {
			targetPlayer.sendSystemMessage(Component.translatable("caw.message.faction.transferred_to_u",
			player.getGameProfile().getName(),
			FactionDataManager.get().factions.get(factionUUID).getFactionName()
			));
		});
		return Either.left(
		new Tuple<>(FactionDataManager.get().getFaction(factionUUID).orElseThrow(),
		FactionDataManager.get().getFaction(factionUUID).orElseThrow().members.get(targetPlayerUUID))
		);
	}
	
	public static Optional<Component> tryKickPlayer(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be owner or admin
		if (!FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_owner"));
		}
		// that member must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(targetPlayerUUID, factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction.other"));
		}
		// that member must not be owner or admin
		if (FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(targetPlayerUUID, factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.admin_owner_cannot_be_kicked"));
		}
		return Optional.empty();
	}
	
	/**
	 * Kicks a player from a faction.
	 *
	 * @param player           The player kicking the other player.
	 * @param factionUUID      The UUID of the faction to kick the player from.
	 * @param targetPlayerUUID The UUID of the player to kick.
	 * @return The name of the player that was kicked or an error message if something went wrong.<br>
	 * (Can't return faction member data because the player is no longer in the faction)
	 */
	public static Either<String, Component> kickPlayer(ServerPlayer player, UUID factionUUID, UUID targetPlayerUUID) {
		Optional<Component> error = tryKickPlayer(player, factionUUID, targetPlayerUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		String targetPlayerName = OfflinePlayerDatabase.get().getName(targetPlayerUUID).orElse("Unknown[" + targetPlayerUUID.toString().substring(0, 8) + "]");
		FactionDataManager.get().removePlayerFromFaction(targetPlayerUUID, factionUUID, true);
		sendUpdateToOnlineMembers(factionUUID);
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(
		factionUUID,
		Component.translatable("caw.message.faction.player_kicked",
		targetPlayerName,
		player.getGameProfile().getName(),
		FactionDataManager.get().factions.get(factionUUID).getFactionName()
		)
		);
		return Either.left(targetPlayerName);
	}
	
	public static Optional<Component> tryDisband(ServerPlayer player, UUID factionUUID) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be owner
		if (!FactionDataManager.get().isOwnerOfFaction(player.getUUID(), factionUUID)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_owner"));
		}
		// faction must no longer own any claims
		if (!FactionClaimDataManager.get().getClaimsOf(factionUUID).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.no_disband_with_claim"));
		}
		return Optional.empty();
	}
	
	/**
	 * Disbands a faction.
	 *
	 * @param player      The player disbanding the faction.
	 * @param factionUUID The UUID of the faction to disband.
	 * @return The name of the faction that was disbanded or an error message if something went wrong.
	 */
	public static Either<String, Component> disband(ServerPlayer player, UUID factionUUID) {
		Optional<Component> error = tryDisband(player, factionUUID);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		String factionName = FactionDataManager.get().factions.get(factionUUID).getFactionName();
		//send before actual disband because once disbanded
		//the faction will be removed from the data manager
		FactionDataManager.get().sendMessageToAllPlayersOnlineInFaction(factionUUID,
		Component.translatable("caw.message.faction.disbanded", factionName, player.getGameProfile().getName())
		);
		UUID[] playersOfFaction = FactionDataManager.get().getFaction(factionUUID).orElseThrow().members.keySet().toArray(new UUID[0]);
		FactionDataManager.get().disbandFaction(factionUUID);
		for (UUID playerUUID : playersOfFaction) {
			sendUpdateIfOnline(playerUUID);
		}
		return Either.left(factionName);
	}
	
	public static Optional<Component> trySetDiplomaticRelationship(ServerPlayer player, UUID otherFaction, int relationship) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(otherFaction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// player must be in a primary faction
		if (FactionDataManager.get().getPrimaryFaction(player.getUUID()).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.no_primary_faction"));
		}
		UUID primaryFaction = FactionDataManager.get().getPrimaryFaction(player.getUUID()).orElseThrow().getFactionUUID();
		// must be at least admin of their own faction
		if (!FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(player.getUUID(), primaryFaction)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_admin"));
		}
		// relationship must be valid
		if (relationship < -1 || relationship > 1) {
			return Optional.of(Component.translatable("caw.errors.faction.invalid_relationship"));
		}
		return Optional.empty();
	}
	
	public static Either<Pair<String, Integer>, Component> setDiplomaticRelationship(ServerPlayer player, UUID otherFaction, int relationship) {
		Optional<Component> error = trySetDiplomaticRelationship(player, otherFaction, relationship);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		String otherFactionName = FactionDataManager.get().factions.get(otherFaction).getFactionName();
		UUID playerFactionUUID = FactionDataManager.get().getPrimaryFaction(player.getUUID()).orElseThrow().getFactionUUID();
		FactionDataManager.get().setDiplomaticRelationship(playerFactionUUID, otherFaction, relationship);
		sendUpdateToOnlineMembers(playerFactionUUID);
		return Either.left(new Pair<>(otherFactionName, relationship));
	}
	
	public static Optional<Component> trySetFakePlayerPolicy(ServerPlayer player, UUID faction, int policy) {
		if (PlayerThrottleUtil.throttlePlayerOperation(player.getUUID())) {
			return Optional.of(PlayerThrottleUtil.getErrorMessage());
		}
		// the function must exist
		if (FactionDataManager.get().getFaction(faction).isEmpty()) {
			return Optional.of(Component.translatable("caw.errors.faction.not_found"));
		}
		// must be in the faction
		if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), faction)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_in_faction"));
		}
		// must be at least admin
		if (!FactionDataManager.get().isPlayerAdminOrOwnerOfFaction(player.getUUID(), faction)) {
			return Optional.of(Component.translatable("caw.errors.faction.not_admin"));
		}
		// policy must be valid
		if (policy < -1 || policy > 1) {
			return Optional.of(Component.translatable("caw.errors.faction.invalid_policy"));
		}
		return Optional.empty();
	}
	
	public static Either<Pair<String, Integer>, Component> setFakePlayerPolicy(ServerPlayer player, UUID faction, int policy) {
		Optional<Component> error = trySetFakePlayerPolicy(player, faction, policy);
		if (error.isPresent()) {
			return Either.right(error.get());
		}
		String factionName = FactionDataManager.get().factions.get(faction).getFactionName();
		FactionDataManager.get().setFakePlayerPolicy(faction, policy);
		sendUpdateToOnlineMembers(faction);
		return Either.left(new Pair<>(factionName, policy));
	}
	
	private static void sendUpdateIfOnline(UUID playerUUID) {
		getPlayerIfOnline(playerUUID).ifPresent(FactionPacketGenerator::scheduleSend);
	}
	
	static Optional<ServerPlayer> getPlayerIfOnline(UUID playerUUID) {
		return Optional.ofNullable(FactionDataManager.SERVER.getPlayerList().getPlayer(playerUUID));
	}
	
	private static void sendUpdateToOnlineMembers(UUID factionUUID) {
		FactionDataManager.get().getFaction(factionUUID)
		.orElseThrow()
		.members
		.keySet()
		.stream()
		.map(FactionPacketHandler::getPlayerIfOnline)
		.filter(Optional::isPresent)
		.map(Optional::get)
		.forEach(FactionPacketGenerator::scheduleSend);
	}
}
