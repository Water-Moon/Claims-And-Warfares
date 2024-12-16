package net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconPacketHandler;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;

import java.util.UUID;
import java.util.function.Supplier;

public class PTSBeaconInstructionPacket {
	
	int type;
	
	public PTSBeaconInstructionPacket(int type){
		this.type = type;
	}
	
	public static PTSBeaconInstructionPacket fromBytes(FriendlyByteBuf byteBuf){
		return new PTSBeaconInstructionPacket(byteBuf.readVarInt());
	}
	
	public static void toBytes(PTSBeaconInstructionPacket packet, FriendlyByteBuf byteBuf){
		byteBuf.writeVarInt(packet.type);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if(player == null) return;
			BeaconPacketHandler.handleInstruction(player, this.type);
		});
		context.setPacketHandled(true);
	}
	
}
