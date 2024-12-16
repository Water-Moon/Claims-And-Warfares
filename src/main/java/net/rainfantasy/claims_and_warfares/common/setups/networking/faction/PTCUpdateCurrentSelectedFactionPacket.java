package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.data_types.ClientFactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionInfo;

import java.util.function.Supplier;

public class PTCUpdateCurrentSelectedFactionPacket {
	
	FactionInfo faction;
	
	public PTCUpdateCurrentSelectedFactionPacket(FactionInfo faction) {
		this.faction = faction;
	}
	
	public static void toBytes(PTCUpdateCurrentSelectedFactionPacket packet, FriendlyByteBuf byteBuf) {
		packet.faction.toBytes(byteBuf);
	}
	
	public static PTCUpdateCurrentSelectedFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		FactionInfo faction = new FactionInfo().fromBytes(byteBuf);
		return new PTCUpdateCurrentSelectedFactionPacket(faction);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientDataManager.setCurrentSelectedFaction(new ClientFactionData(faction));
		});
		context.setPacketHandled(true);
	}
}
