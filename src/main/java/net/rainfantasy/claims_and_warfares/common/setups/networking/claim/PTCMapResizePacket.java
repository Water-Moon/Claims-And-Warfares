package net.rainfantasy.claims_and_warfares.common.setups.networking.claim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PTCMapResizePacket {
	
	int radiusX;
	int radiusZ;
	
	public PTCMapResizePacket(int radiusX, int radiusZ) {
		this.radiusX = radiusX;
		this.radiusZ = radiusZ;
	}
	
	public static PTCMapResizePacket fromBytes(FriendlyByteBuf byteBuf) {
		int radiusX = byteBuf.readVarInt();
		int radiusZ = byteBuf.readVarInt();
		return new PTCMapResizePacket(radiusX, radiusZ);
	}
	
	public static void toBytes(PTCMapResizePacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(packet.radiusX);
		byteBuf.writeVarInt(packet.radiusZ);
	}
	
	public void execute(Supplier<Context> supplier){
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.setClaimInfoSize(this.radiusX, this.radiusZ);
		});
		context.setPacketHandled(true);
	}
}
