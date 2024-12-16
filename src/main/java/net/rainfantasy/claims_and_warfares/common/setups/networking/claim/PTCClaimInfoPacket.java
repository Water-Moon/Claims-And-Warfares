package net.rainfantasy.claims_and_warfares.common.setups.networking.claim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimedChunkInfo;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCClaimInfoPacket {
	
	public int offsetX;
	public int offsetZ;
	public ClaimedChunkInfo info;
	
	public PTCClaimInfoPacket(int offsetX, int offsetZ, ClaimedChunkInfo info) {
		this.offsetX = offsetX;
		this.offsetZ = offsetZ;
		this.info = info;
	}
	
	public static @NotNull PTCClaimInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		int offsetX = byteBuf.readVarInt();
		int offsetZ = byteBuf.readVarInt();
		ClaimedChunkInfo info = new ClaimedChunkInfo().fromBytes(byteBuf);
		return new PTCClaimInfoPacket(offsetX, offsetZ, info);
	}
	
	public static void toBytes(PTCClaimInfoPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(packet.offsetX);
		byteBuf.writeVarInt(packet.offsetZ);
		packet.info.toBytes(byteBuf);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWClientGUIManager.updateClaimInfo(this);
		context.setPacketHandled(true);
	}
}
