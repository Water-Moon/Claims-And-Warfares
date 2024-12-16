package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionInfo;

import java.util.function.Supplier;

public class PTCFactionInfoPacket {
	
	final FactionInfo factionInfo;
	
	public PTCFactionInfoPacket(FactionInfo factionInfo) {
		this(factionInfo, false);
	}
	public PTCFactionInfoPacket(FactionInfo factionInfo, boolean trimmed) {
		this.factionInfo = factionInfo;
		if(trimmed){
			this.factionInfo.trim();
		}
	}
	
	public static PTCFactionInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTCFactionInfoPacket(new FactionInfo().fromBytes(byteBuf));
	}
	
	public static void toBytes(PTCFactionInfoPacket packet, FriendlyByteBuf byteBuf) {
		packet.factionInfo.toBytes(byteBuf);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientDataManager.addFactionData(
				new ClientFactionData(this.factionInfo)
			);
		});
		context.setPacketHandled(true);
	}
}
