package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCOpenLeaveFactionPacket {
	
	public PTCOpenLeaveFactionPacket() {
	}
	
	public static @NotNull PTCOpenLeaveFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCOpenLeaveFactionPacket();
	}
	
	public static void toBytes(PTCOpenLeaveFactionPacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(CAWClientDataManager::clearAllLocalData);
		CAWConstants.execute(CAWClientGUIManager::openLeaveFactionPage);
		context.setPacketHandled(true);
	}
}
