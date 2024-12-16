package net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Tuple;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCUpdateClientUUIDPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.*;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FactionPacketGenerator {
	
	
	private static void sendPlayerList(ServerPlayer player, MinecraftServer server) {
		PlayerList playerList = server.getPlayerList();
		List<ServerPlayerInfo> playerInfoList = new ArrayList<>();
		playerList.getPlayers().forEach(p -> {
			List<FactionData> factions = FactionDataManager.get().getFactionsForPlayer(p.getUUID());
			playerInfoList.add(
			new ServerPlayerInfo(p.getUUID(),
			p.getName().getString(),
			factions.stream().map(FactionData::getFactionUUID).collect(Collectors.toSet())
			));
		});
		PTCServerPlayerInfoPacket packet = new PTCServerPlayerInfoPacket(playerInfoList);
		ChannelRegistry.sendToClient(player, packet);
	}
	
	private static void sendFactionInfo(ServerPlayer player, MinecraftServer server) {
		FactionDataManager.get().getAllFactions().forEach(factionData -> {
			FactionInfo info = new FactionInfo(factionData);
			if (FactionDataManager.get().isPlayerInFaction(player.getUUID(), factionData.getFactionUUID())) {
				ChannelRegistry.sendToClient(player, new PTCFactionInfoPacket(info));
			} else {
				ChannelRegistry.sendToClient(player, new PTCFactionInfoPacket(info, true));
			}
		});
	}
	
	public static void sendKnownInvitationsTo(ServerPlayer player) {
		UUID playerUUID = player.getUUID();
		Stream.concat(
		FactionDataManager.get().getInvitationsToPlayer(playerUUID).stream(),
		FactionDataManager.get().getInvitationsFromPlayer(playerUUID).stream()
		).map(InvitationInfo::new)
		.forEach(info -> ChannelRegistry.sendToClient(player, new PTCInvitationInfoPacket(info)));
	}
	
	public static void sendClientUUIDInfo(ServerPlayer player) {
		ChannelRegistry.sendToClient(player, new PTCUpdateClientUUIDPacket(player));
	}
	
	public static void sendClientSelectedFactionInfo(ServerPlayer player) {
		Optional<FactionData> selectedFaction = FactionDataManager.get().getPrimaryFaction(player.getUUID());
		selectedFaction.ifPresent(
		factionData -> ChannelRegistry.sendToClient(player, new PTCUpdateCurrentSelectedFactionPacket(new FactionInfo(factionData)))
		);
	}
	
	public static List<Tuple<UUID, String>> collectRelatedOfflinePlayers(ServerPlayer player) {
		MinecraftServer server = player.getServer();
		if (server == null) return new ArrayList<>();
		ArrayList<Tuple<UUID, String>> list = new ArrayList<>();
		
		//other players in the same faction
		FactionDataManager.get().getFactionsForPlayer(player.getUUID()).forEach(factionData -> {
			factionData.getMembers().forEach(uuid -> {
				if (!isPlayerOnline(uuid, server)) {
					String name = factionData.getDataOf(uuid).getKnownPlayerName();
					list.add(new Tuple<>(uuid, name));
				}
			});
		});
		
		//players who have invitations to the player
		FactionDataManager.get().getInvitationsToPlayer(player.getUUID()).forEach(inviteInfo -> {
			if (!isPlayerOnline(inviteInfo.getFromPlayerUUID(), server)) {
				list.add(new Tuple<>(inviteInfo.getFromPlayerUUID(), inviteInfo.getFromPlayerName()));
			}
		});
		
		//players who have invitations from the player
		FactionDataManager.get().getInvitationsFromPlayer(player.getUUID()).forEach(inviteInfo -> {
			if (!isPlayerOnline(inviteInfo.getToPlayerUUID(), server)) {
				list.add(new Tuple<>(inviteInfo.getToPlayerUUID(), inviteInfo.getToPlayerName()));
			}
		});
		
		return list;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isPlayerOnline(UUID playerUUID, MinecraftServer server) {
		return server.getPlayerList().getPlayer(playerUUID) != null;
	}
	
	public static void sendOfflinePlayerInfo(ServerPlayer player) {
		List<Tuple<UUID, String>> list = collectRelatedOfflinePlayers(player);
		list.forEach(tuple -> ChannelRegistry.sendToClient(player, new PTCOfflinePlayerInfoPacket(tuple.getA(), tuple.getB())));
	}
	
	public static void sendDiplomaticRelationshipInfo(ServerPlayer player) {
		FactionDataManager.get().getPrimaryFaction(player.getUUID()).ifPresent(factionData -> {
			factionData.getDiplomaticRelationships().forEach((otherFaction, relationship) -> {
				ChannelRegistry.sendToClient(player, new PTCDiplomaticRelationshipData(otherFaction, relationship.getRelationship()));
			});
		});
	}
	
	public static void scheduleSend(@Nullable ServerPlayer player) {
		if (player == null) return;
		MinecraftServer server = player.getServer();
		if (server == null) return;
		server.executeIfPossible(() -> {
			ChannelRegistry.sendToClient(player, new PTCRequestClientRefreshPacket());
			sendClientUUIDInfo(player);
			sendPlayerList(player, server);
			sendFactionInfo(player, server);
			sendKnownInvitationsTo(player);
			sendClientSelectedFactionInfo(player);
			sendDiplomaticRelationshipInfo(player);
			sendOfflinePlayerInfo(player);
			//...
		});
	}
}
