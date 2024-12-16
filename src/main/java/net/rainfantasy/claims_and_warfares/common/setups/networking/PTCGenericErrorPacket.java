package net.rainfantasy.claims_and_warfares.common.setups.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCGenericErrorPacket {
	
	Component errorMessage;
	
	public PTCGenericErrorPacket(Component errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public static PTCGenericErrorPacket fromBytes(FriendlyByteBuf byteBuf) {
		Component errorMessage = byteBuf.readComponent();
		return new PTCGenericErrorPacket(errorMessage);
	}
	
	public static void toBytes(PTCGenericErrorPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeComponent(packet.errorMessage);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> CAWClientGUIManager.setLastMessage(this.errorMessage));
		context.setPacketHandled(true);
	}
	
}
