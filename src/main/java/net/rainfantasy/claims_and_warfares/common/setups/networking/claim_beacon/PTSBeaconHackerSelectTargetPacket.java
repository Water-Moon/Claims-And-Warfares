package net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;

import java.util.function.Supplier;

public class PTSBeaconHackerSelectTargetPacket {
	
	BlockPos target;
	
	public PTSBeaconHackerSelectTargetPacket(BlockPos target) {
		this.target = target;
	}
	
	public static PTSBeaconHackerSelectTargetPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSBeaconHackerSelectTargetPacket(byteBuf.readBlockPos());
	}
	
	public static void toBytes(PTSBeaconHackerSelectTargetPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeBlockPos(packet.target);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			BeaconHackerBlockEntity.setTargetPos(player, this.target);
		});
		context.setPacketHandled(true);
	}
	
}
