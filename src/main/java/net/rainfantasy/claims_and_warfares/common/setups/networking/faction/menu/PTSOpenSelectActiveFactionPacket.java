package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.function.Supplier;

public class PTSOpenSelectActiveFactionPacket {
	
	public PTSOpenSelectActiveFactionPacket() {
	}
	
	public static PTSOpenSelectActiveFactionPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSOpenSelectActiveFactionPacket();
	}
	
	public static void toBytes(PTSOpenSelectActiveFactionPacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ChannelRegistry.reply(context, new PTCOpenSelectActiveFactionPacket());
			FactionPacketGenerator.scheduleSend(context.getSender());
		});
		context.setPacketHandled(true);
	}
	
}
