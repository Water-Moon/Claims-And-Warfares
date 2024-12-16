package net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.INetworkInfo;


/**
 * Represents the information of a claimed chunk
 * to be sent to client for display purpose, not actually stored in the world
 */
public class ClaimedChunkInfo implements INetworkInfo<ClaimedChunkInfo> {
	
	public final int ChunkX;
	public final int ChunkZ;
	public final Component Name;
	public final int color;
	
	public ClaimedChunkInfo() {
		ChunkX = 0;
		ChunkZ = 0;
		Name = Component.empty();
		color = 0;
	}
	
	public ClaimedChunkInfo(int chunkX, int chunkZ, String name, int color) {
		ChunkX = chunkX;
		ChunkZ = chunkZ;
		Name = Component.literal(name);
		this.color = color;
	}
	
	
	public ClaimedChunkInfo(int chunkX, int chunkZ, Component name, int color) {
		ChunkX = chunkX;
		ChunkZ = chunkZ;
		Name = name;
		this.color = color;
	}
	
	@Override
	public void toBytes(FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(ChunkX);
		byteBuf.writeVarInt(ChunkZ);
		byteBuf.writeComponent(Name);
		byteBuf.writeVarInt(color);
	}
	
	@Override
	public ClaimedChunkInfo fromBytes(FriendlyByteBuf byteBuf) {
		int chunkX = byteBuf.readVarInt();
		int chunkZ = byteBuf.readVarInt();
		Component name = byteBuf.readComponent();
		int color = byteBuf.readVarInt();
		return new ClaimedChunkInfo(chunkX, chunkZ, name, color);
	}
}
