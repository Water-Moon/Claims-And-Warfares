package net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon_upgrade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_block_entities.BeaconDiplomaticUpgradeBE;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_menus.BeaconDiplomaticUpgradeMenu;

import java.util.function.Supplier;

public class PTSDiplomaticUpgradeSetPacket {
	
	int type;
	int value;
	
	public PTSDiplomaticUpgradeSetPacket(int type, int value) {
		this.type = type;
		this.value = value;
	}
	
	public static PTSDiplomaticUpgradeSetPacket fromBytes(FriendlyByteBuf byteBuf) {
		return new PTSDiplomaticUpgradeSetPacket(byteBuf.readVarInt(), byteBuf.readVarInt());
	}
	
	public static void toBytes(PTSDiplomaticUpgradeSetPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(packet.type);
		byteBuf.writeVarInt(packet.value);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			if (!(player.containerMenu instanceof BeaconDiplomaticUpgradeMenu menu)) return;
			BeaconDiplomaticUpgradeBE be = menu.block;
			be.setPermission(player, this.type, this.value);
		});
		context.setPacketHandled(true);
	}
	
}
