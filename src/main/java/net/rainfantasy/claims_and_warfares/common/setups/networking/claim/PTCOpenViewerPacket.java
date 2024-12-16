package net.rainfantasy.claims_and_warfares.common.setups.networking.claim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCOpenViewerPacket {
	
	int radiusX;
	int radiusZ;
	
	public PTCOpenViewerPacket(int radiusX, int radiusZ) {
		this.radiusX = radiusX;
		this.radiusZ = radiusZ;
	}
	
	public static @NotNull PTCOpenViewerPacket fromBytes(FriendlyByteBuf byteBuf) {
		int radiusX = byteBuf.readVarInt();
		int radiusZ = byteBuf.readVarInt();
		return new PTCOpenViewerPacket(radiusX, radiusZ);
	}
	
	public static void toBytes(PTCOpenViewerPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(packet.radiusX);
		byteBuf.writeVarInt(packet.radiusZ);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWClientGUIManager.openClaimMap(this.radiusX, this.radiusZ);
		context.setPacketHandled(true);
	}
}
