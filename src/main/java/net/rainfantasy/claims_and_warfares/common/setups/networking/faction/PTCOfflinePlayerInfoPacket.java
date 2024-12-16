package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.data_types.OfflinePlayerData;

import java.util.UUID;
import java.util.function.Supplier;

public class PTCOfflinePlayerInfoPacket {
	
	UUID uuid;
	String name;
	
	public PTCOfflinePlayerInfoPacket(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}
	
	public static PTCOfflinePlayerInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID uuid = byteBuf.readUUID();
		String name = byteBuf.readUtf();
		return new PTCOfflinePlayerInfoPacket(uuid, name);
	}
	
	public static void toBytes(PTCOfflinePlayerInfoPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.uuid);
		byteBuf.writeUtf(packet.name);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientDataManager.addOfflinePlayerData(new OfflinePlayerData(uuid, name));
		});
		context.setPacketHandled(true);
	}
	
}
