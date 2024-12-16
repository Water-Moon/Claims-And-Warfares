package net.rainfantasy.claims_and_warfares.common.functionalities.mapper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.BeaconHackerMenu;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCMapInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCMapResizePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.antlr.v4.runtime.misc.Triple;
import org.joml.Vector2i;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapPacketGenerator {
	
	public static ConcurrentHashMap<UUID, Triple<Long, Integer, Integer>> recentlyOpenedMapSize = new ConcurrentHashMap<>();
	
	public static Vector2i toChunkCoords(Vec3 playerPos) {
		return new Vector2i((int) playerPos.x() >> 4, (int) playerPos.z() >> 4);
	}
	
	public static Block getTopBlockAt(Level level, int x, int z) {
		BlockPos pos = new BlockPos(x, level.getMaxBuildHeight(), z);
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
			(!state.isAir()) //&&
				//state.isSolidRender(level, pos)
			) {
				return state.getBlock();
			}
			pos = pos.below();
		}
		return Blocks.AIR;
	}
	public static Tuple<BlockState, BlockPos> getTopBlockWithInfo(Level level, int x, int z) {
		BlockPos pos = new BlockPos(x, level.getMaxBuildHeight(), z);
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
			(!state.isAir()) //&&
				//state.isSolidRender(level, pos)
			) {
				return new Tuple<>(state, pos);
			}
			pos = pos.below();
		}
		return new Tuple<>(Blocks.AIR.defaultBlockState(), pos);
	}

	
	/**
	 * Get the map info around the player
	 *
	 * @param player       The player
	 * @param offsetChunkX The offset in chunks on the x axis
	 * @param offsetChunkZ The offset in chunks on the z axis
	 * @param topX         The x coordinate of the top left corner of the map
	 * @param topZ         The z coordinate of the top left corner of the map
	 * @return The map info packet
	 */
	public static PTCMapInfoPacket getInfo(ServerPlayer player, Vector2i centerChunkCoord, int offsetChunkX, int offsetChunkZ, int topX, int topZ) {
		int[][] data = new int[16][16];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int x = centerChunkCoord.x() * 16 + i + offsetChunkX * 16;
				int z = centerChunkCoord.y() * 16 + j + offsetChunkZ * 16;
				//data[i][j] = ColorUtil.getColor(getTopBlockAt(player.level(), x, z));
				Tuple<BlockState, BlockPos> info = getTopBlockWithInfo(player.level(), x, z);
				data[i][j] = ColorUtil.getColor(player.level(), info.getA(), info.getB());
			}
		}
		int topLeftWorldX = (centerChunkCoord.x() + offsetChunkX) << 4;
		int topLeftWorldZ = (centerChunkCoord.y() + offsetChunkZ) << 4;
		int offsetX = topLeftWorldX - topX;
		int offsetZ = topLeftWorldZ - topZ;
		
		return new PTCMapInfoPacket(offsetX, offsetZ, topX, topZ, data);
	}
	
	@SuppressWarnings("DuplicatedCode")
	public static void scheduleSend(ServerPlayer player, int radiusX, int radiusZ) {
		scheduleSend(player, toChunkCoords(player.position()), radiusX, radiusZ);
	}
	@SuppressWarnings("DuplicatedCode")
	public static void scheduleSend(ServerPlayer player, Vector2i chunkCoords, int radiusX, int radiusZ) {
		if (player.getServer() == null) return;
		player.getServer().executeIfPossible(() -> {
			ChannelRegistry.sendToClient(player, new PTCMapResizePacket(radiusX, radiusZ));
			recentlyOpenedMapSize.put(player.getUUID(), new Triple<>(System.currentTimeMillis(), radiusX, radiusZ));
			int topX = 16 * (chunkCoords.x() - radiusX);
			int topZ = 16 * (chunkCoords.y() - radiusZ);
			Thread sendThread = new Thread(() -> {
				for (int i = -radiusX; i <= radiusX; i++) {
					boolean flag = false;
					for (int j = -radiusZ; j <= radiusZ; j++) {
						
						// So to prevent sending too many packets at once
						try{
							Thread.sleep(1);
						}catch (Exception e){
							CAWConstants.LOGGER.error("Error in thread", e);
						}
						int finalI = i;
						int finalJ = j;
						if(!CAWConstants.execute(() ->
							ChannelRegistry.sendToClient(player, getInfo(player, chunkCoords, finalI, finalJ, topX, topZ)))){
							flag = true;
							break;
						}
					}
					if(flag) break;
				}
			});
			sendThread.setName("Map Sender (to:" + player.getScoreboardName() + ")");
			sendThread.start();
		});
	}
	
	@SuppressWarnings("DuplicatedCode")
	public static void sendToPlayerWithOpenMapScreen(MinecraftServer server){
		server.getPlayerList().getPlayers().forEach(player -> {
			if(player.containerMenu instanceof ClaimBeaconMenu claimBeaconMenu){
				scheduleSend(player, CoordUtil.blockToChunk(claimBeaconMenu.block.getBlockPos()), 3, 3);
			}else if(player.containerMenu instanceof BeaconHackerMenu beaconHackerMenu){
				scheduleSend(player, CoordUtil.blockToChunk(beaconHackerMenu.block.getBlockPos()), 2, 2);
			}else {
				Optional.ofNullable(MapPacketGenerator.recentlyOpenedMapSize.get(player.getUUID())).ifPresent(entry -> {
					if(entry.a < (System.currentTimeMillis() + 60*60*1000)) {
						scheduleSend(player, entry.b, entry.c);
					}
				});
			}
		});
	}
	
}
