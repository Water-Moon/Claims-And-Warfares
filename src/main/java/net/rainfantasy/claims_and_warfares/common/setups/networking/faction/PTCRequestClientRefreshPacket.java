package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;

import java.util.function.Supplier;

public class PTCRequestClientRefreshPacket {
	
	public PTCRequestClientRefreshPacket() {
	}
	
	public static PTCRequestClientRefreshPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCRequestClientRefreshPacket();
	}
	
	public static void toBytes(PTCRequestClientRefreshPacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(CAWClientDataManager::clearAllLocalData);
		context.setPacketHandled(true);
	}
}
