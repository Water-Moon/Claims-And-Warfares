package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PTCFactionCreatedPacket {
	
	String factionName;
	
	public PTCFactionCreatedPacket(String factionName) {
		this.factionName = factionName;
	}
	
	public static @NotNull PTCFactionCreatedPacket fromBytes(FriendlyByteBuf byteBuf) {
		String factionName = byteBuf.readUtf();
		return new PTCFactionCreatedPacket(factionName);
	}
	
	public static void toBytes(PTCFactionCreatedPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUtf(packet.factionName);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.factionCreateSuccess(this.factionName);
		});
		context.setPacketHandled(true);
	}
}
