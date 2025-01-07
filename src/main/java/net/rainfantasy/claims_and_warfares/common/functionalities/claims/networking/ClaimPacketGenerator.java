package net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.mapper.MapPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.BeaconHackerMenu;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCClaimInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.rainfantasy.claims_and_warfares.common.functionalities.mapper.MapPacketGenerator.toChunkCoords;

public class ClaimPacketGenerator {
	
	public static PTCClaimInfoPacket getClaimInfoPacket(int chunkX, int chunkZ, int topX, int leftZ, ClaimData data) {
		int chunkTopX = topX >> 4;
		int chunkLeftZ = leftZ >> 4;
		Component name = Component.translatable("caw.gui.label.claim_unknown");
		if (data.hasFeature(FactionOwnedClaimFeature.class)) {
			UUID factionUUID = ((FactionOwnedClaimFeature) data.getFeature(FactionOwnedClaimFeature.class).orElseThrow()).getFactionUUID();
			name = Component.translatable("caw.gui.label.claim_faction",
			FactionDataManager.get().getFaction(factionUUID).map(FactionData::getFactionName).map(Component::literal).orElse(Component.translatable("caw.gui.label.claim_unknown")));
		} else {
			for (AbstractClaimFeature feature : data.getAllFeatures()) {
				if (!feature.getDisplayName().getString().isEmpty()) {
					name = feature.getDisplayName();
					break;
				}
			}
		}
		ClaimedChunkInfo info = new ClaimedChunkInfo(chunkX, chunkZ, name, data.getClaimColor());
		return new PTCClaimInfoPacket(chunkX - chunkTopX, chunkZ - chunkLeftZ, info);
	}
	
	public static Optional<PTCClaimInfoPacket> getInfo(ServerPlayer player, Vector2i chunkCoords, int offsetChunkX, int offsetChunkZ, int topX, int leftZ) {
		int x = chunkCoords.x() + offsetChunkX;
		int z = chunkCoords.y() + offsetChunkZ;
		List<ClaimData> data = ClaimDataManager.get().getClaimsAt(player.level(), new Vector2i(x, z));
		if (!data.isEmpty()) {
			return Optional.of(getClaimInfoPacket(x, z, topX, leftZ, data.get(0)));
		}
		return Optional.empty();
	}
	
	public static void scheduleSend(ServerPlayer player, int radiusX, int radiusZ) {
		Vector2i chunkCoords = toChunkCoords(player.position());
		scheduleSend(player, chunkCoords, radiusX, radiusZ);
	}
	
	@SuppressWarnings("DuplicatedCode")
	public static void scheduleSend(ServerPlayer player, Vector2i chunkCoords, int radiusX, int radiusZ) {
		if (player.getServer() == null) return;
		player.getServer().executeIfPossible(() -> {
			int topX = 16 * (chunkCoords.x() - radiusX);
			int leftZ = 16 * (chunkCoords.y() - radiusZ);
			
			Thread sendThread = new Thread(() -> {
				for (int i = -radiusX; i <= radiusX; i++) {
					boolean flag = false;
					for (int j = -radiusZ; j <= radiusZ; j++) {
						int finalI = i;
						int finalJ = j;
						
						if (!CAWConstants.execute(() -> {
							Optional<PTCClaimInfoPacket> packet = getInfo(player, chunkCoords, finalI, finalJ, topX, leftZ);
							packet.ifPresent(p -> ChannelRegistry.sendToClient(player, p));
						})) {
							flag = true;
							break;
						}
					}
					if (flag) {
						break;
					}
				}
			});
			sendThread.setName("Claim Sender (to:" + player.getScoreboardName() + ")");
			sendThread.start();
		});
	}
	
	@SuppressWarnings("DuplicatedCode")
	public static void sendToPlayerWithOpenMapScreen(MinecraftServer server) {
		server.getPlayerList().getPlayers().forEach(player -> {
			if (player.containerMenu instanceof ClaimBeaconMenu claimBeaconMenu) {
				scheduleSend(player, CoordUtil.blockToChunk(claimBeaconMenu.block.getBlockPos()), 3, 3);
			} else if (player.containerMenu instanceof BeaconHackerMenu beaconHackerMenu) {
				scheduleSend(player, CoordUtil.blockToChunk(beaconHackerMenu.block.getBlockPos()), 2, 2);
			} else {
				Optional.ofNullable(MapPacketGenerator.recentlyOpenedMapSize.get(player.getUUID())).ifPresent(entry -> {
					if (entry.a < (System.currentTimeMillis() + 60 * 60 * 1000)) {
						scheduleSend(player, entry.b, entry.c);
					}
				});
			}
		});
	}
}
