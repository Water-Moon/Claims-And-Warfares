package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class PTCOpenTransferFactionConfirmPacket {
	
	UUID factionUUID;
	UUID playerUUID;
	
	public PTCOpenTransferFactionConfirmPacket(UUID factionUUID, UUID playerUUID) {
		this.factionUUID = factionUUID;
		this.playerUUID = playerUUID;
	}
	
	public static @NotNull PTCOpenTransferFactionConfirmPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCOpenTransferFactionConfirmPacket(byteBuf.readUUID(), byteBuf.readUUID());
	}
	
	public static void toBytes(PTCOpenTransferFactionConfirmPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeUUID(packet.playerUUID);
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.openTransferFactionConfirmPage(factionUUID, playerUUID);
		});
		context.setPacketHandled(true);
	}
}
