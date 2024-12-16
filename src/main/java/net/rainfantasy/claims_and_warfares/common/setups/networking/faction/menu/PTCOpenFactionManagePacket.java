package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCOpenFactionManagePacket {
	
	public PTCOpenFactionManagePacket() {
	}
	
	public static @NotNull PTCOpenFactionManagePacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCOpenFactionManagePacket();
	}
	
	public static void toBytes(PTCOpenFactionManagePacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(CAWClientDataManager::clearAllLocalData);
		CAWConstants.execute(CAWClientGUIManager::openFactionManagementPage);
		context.setPacketHandled(true);
	}
}
