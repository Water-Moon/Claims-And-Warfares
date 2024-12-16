package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;

import java.util.function.Supplier;

public class PTCSetRelationshipSuccessPacket {
	
	String otherFactionName;
	int newRelationship;
	
	public PTCSetRelationshipSuccessPacket(String otherFactionName, int newRelationship) {
		this.otherFactionName = otherFactionName;
		this.newRelationship = newRelationship;
	}
	
	public static PTCSetRelationshipSuccessPacket fromBytes(FriendlyByteBuf buf) {
		String otherFactionName = buf.readUtf();
		int newRelationship = buf.readVarInt();
		return new PTCSetRelationshipSuccessPacket(otherFactionName, newRelationship);
	}
	
	public static void toBytes(PTCSetRelationshipSuccessPacket packet, FriendlyByteBuf buf) {
		buf.writeUtf(packet.otherFactionName);
		buf.writeVarInt(packet.newRelationship);
	}
	
	public void execute(Supplier<Context> supplier){
		Context ctx = supplier.get();
		CAWConstants.execute(() -> {
			Component relationship = Component.translatable("caw.gui.common.unknown");
			switch (newRelationship) {
				case 0:
					relationship = Component.translatable("caw.string.faction.neutral");
					break;
				case 1:
					relationship = Component.translatable("caw.string.faction.ally");
					break;
				case -1:
					relationship = Component.translatable("caw.string.faction.hostile");
					break;
			}
			CAWClientGUIManager.setLastMessage(
				Component.translatable("caw.message.faction.relationship_set", otherFactionName, relationship)
			);
		});
		ctx.setPacketHandled(true);
	}
}
