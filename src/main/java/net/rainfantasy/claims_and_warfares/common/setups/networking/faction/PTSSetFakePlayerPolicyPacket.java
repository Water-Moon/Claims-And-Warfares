package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionPacketHandler;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericMessagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;

import java.util.UUID;
import java.util.function.Supplier;

import static net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData.*;

public class PTSSetFakePlayerPolicyPacket {
	
	int policy;
	UUID factionUUID;
	
	public PTSSetFakePlayerPolicyPacket(UUID factionUUID, int policy) {
		this.factionUUID = factionUUID;
		this.policy = policy;
	}
	
	public static PTSSetFakePlayerPolicyPacket fromBytes(FriendlyByteBuf byteBuf) {
		UUID factionUUID = byteBuf.readUUID();
		int relationship = byteBuf.readVarInt();
		return new PTSSetFakePlayerPolicyPacket(factionUUID, relationship);
	}
	
	public static void toBytes(PTSSetFakePlayerPolicyPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(packet.factionUUID);
		byteBuf.writeVarInt(packet.policy);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			FactionPacketHandler.setFakePlayerPolicy(player, factionUUID, policy)
			.ifLeft(data -> {
				Component policyName = switch (data.getSecond()) {
					case FAKE_PLAYER_POLICY_CHECK_UUID -> Component.translatable("caw.string.faction.fake_player.uuid");
					case FAKE_PLAYER_POLICY_ALLOW -> Component.translatable("caw.string.faction.fake_player.allow");
					case FAKE_PLAYER_POLICY_DENY -> Component.translatable("caw.string.faction.fake_player.deny");
					default -> Component.translatable("caw.gui.common.unknown");
				};
				ChannelRegistry.sendToClient(player, new PTCGenericMessagePacket(
				Component.translatable("message.claims_and_warfares.faction.fake_player_policy_set", data.getFirst(), policyName
				)));
			}).ifRight(error -> {
				ChannelRegistry.sendErrorToClient(player, error);
			});
		});
		context.setPacketHandled(true);
	}
}
