package net.rainfantasy.claims_and_warfares.common.setups.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCGenericMessagePacket {
	
	Component message;
	
	public PTCGenericMessagePacket(Component message) {
		this.message = message;
	}
	
	public static PTCGenericMessagePacket fromBytes(FriendlyByteBuf byteBuf) {
		Component errorMessage = byteBuf.readComponent();
		return new PTCGenericMessagePacket(errorMessage);
	}
	
	public static void toBytes(PTCGenericMessagePacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeComponent(packet.message);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> CAWClientGUIManager.setLastMessage(this.message));
		CAWConstants.execute(() -> CAWClientGUIManager.displayMessage(this.message));
		context.setPacketHandled(true);
	}
	
}
