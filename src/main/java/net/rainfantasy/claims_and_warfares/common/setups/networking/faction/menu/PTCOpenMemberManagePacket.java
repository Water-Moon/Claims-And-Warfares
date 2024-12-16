package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCOpenMemberManagePacket {
	
	public PTCOpenMemberManagePacket() {
	}
	
	public static @NotNull PTCOpenMemberManagePacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCOpenMemberManagePacket();
	}
	
	public static void toBytes(PTCOpenMemberManagePacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(CAWClientDataManager::clearAllLocalData);
		CAWConstants.execute(CAWClientGUIManager::openMemberManageScreen);
		context.setPacketHandled(true);
	}
}
