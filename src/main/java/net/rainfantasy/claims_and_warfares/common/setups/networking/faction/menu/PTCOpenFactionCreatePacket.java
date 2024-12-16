package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCOpenFactionCreatePacket {
	
	public PTCOpenFactionCreatePacket() {
	}
	
	public static @NotNull PTCOpenFactionCreatePacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCOpenFactionCreatePacket();
	}
	
	public static void toBytes(PTCOpenFactionCreatePacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(CAWClientDataManager::clearAllLocalData);
		CAWConstants.execute(CAWClientGUIManager::openFactionCreationPage);
		context.setPacketHandled(true);
	}
}
