package net.rainfantasy.claims_and_warfares.common.setups.networking.faction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientDataManager;

import java.util.UUID;
import java.util.function.Supplier;

public class PTCDiplomaticRelationshipData {
	
	public UUID otherFaction;
	public int relationship;
	
	public PTCDiplomaticRelationshipData(UUID otherFaction, int relationship) {
		this.otherFaction = otherFaction;
		this.relationship = relationship;
	}
	
	public static PTCDiplomaticRelationshipData fromBytes(FriendlyByteBuf buf) {
		return new PTCDiplomaticRelationshipData(buf.readUUID(), buf.readInt());
	}
	
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeUUID(otherFaction);
		buf.writeInt(relationship);
	}
	
	public void execute(Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientDataManager.addDiplomaticRelationship(this);
		});
		context.setPacketHandled(true);
	}
	
	@Override
	public String toString() {
		return "PTCDiplomaticRelationshipData{" +
		       "otherFaction=" + otherFaction +
		       ", relationship=" + relationship +
		       '}';
	}
}
