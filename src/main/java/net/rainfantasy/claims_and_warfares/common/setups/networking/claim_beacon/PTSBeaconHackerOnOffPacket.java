package net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;

import java.util.function.Supplier;

public class PTSBeaconHackerOnOffPacket {
	
	boolean isOn;
	
	public PTSBeaconHackerOnOffPacket(boolean isOn) {
		this.isOn = isOn;
	}
	
	public static PTSBeaconHackerOnOffPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSBeaconHackerOnOffPacket(byteBuf.readBoolean());
	}
	
	public static void toBytes(PTSBeaconHackerOnOffPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeBoolean(packet.isOn);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			if (isOn) {
				BeaconHackerBlockEntity.turnOn(player);
			} else {
				BeaconHackerBlockEntity.turnOff(player);
			}
		});
		context.setPacketHandled(true);
	}
}
