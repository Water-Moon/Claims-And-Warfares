package net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.function.Supplier;

public class PTSOpenFactionManagePacket {
	
	public PTSOpenFactionManagePacket() {
	}
	
	public static PTSOpenFactionManagePacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSOpenFactionManagePacket();
	}
	
	public static void toBytes(PTSOpenFactionManagePacket packet, FriendlyByteBuf byteBuf) {
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ChannelRegistry.reply(context, new PTCOpenFactionManagePacket());
			FactionPacketGenerator.scheduleSend(context.getSender());
		});
		context.setPacketHandled(true);
	}
	
}
