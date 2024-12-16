package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.function.Supplier;

public class PTSOpenDisbandFactionPacket {
	
	public PTSOpenDisbandFactionPacket() {
	}
	
	public static PTSOpenDisbandFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSOpenDisbandFactionPacket();
	}
	
	public static void toBytes(PTSOpenDisbandFactionPacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			FactionPacketGenerator.scheduleSend(context.getSender());
			ChannelRegistry.reply(context, new PTCOpenDisbandFactionPacket());
		});
		context.setPacketHandled(true);
	}
	
}
