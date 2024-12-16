package net.rainfantasy.claims_and_warfares.common.setups.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;

import java.util.UUID;
import java.util.function.Supplier;

public class PTCUpdateClientUUIDPacket {
	
	UUID clientUUID;
	
	public PTCUpdateClientUUIDPacket(UUID clientUUID) {
		this.clientUUID = clientUUID;
	}
	
	public PTCUpdateClientUUIDPacket(Player player) {
		this.clientUUID = player.getUUID();
	}
	
	public static PTCUpdateClientUUIDPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID clientUUID = byteBuf.readUUID();
		return new PTCUpdateClientUUIDPacket(clientUUID);
	}
	
	public static void toBytes(PTCUpdateClientUUIDPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.clientUUID);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> CAWClientDataManager.updateClientPlayerUUID(this.clientUUID));
		context.setPacketHandled(true);
	}
	
}
