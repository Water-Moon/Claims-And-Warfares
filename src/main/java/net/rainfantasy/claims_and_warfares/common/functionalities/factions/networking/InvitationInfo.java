package net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionInviteData;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.INetworkInfo;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.SerializableDateTime;

import java.util.UUID;

public class InvitationInfo implements INetworkInfo<InvitationInfo> {
	
	private final UUID fromPlayerUUID;
	private final String fromPlayerName;
	private final UUID toPlayerUUID;
	private final String toPlayerName;
	private final UUID factionUUID;
	private final String factionName;
	private final SerializableDateTime sentTime;
	private final UUID invitationUUID;
	
	public InvitationInfo() {
		this.fromPlayerUUID = null;
		this.fromPlayerName = null;
		this.toPlayerUUID = null;
		this.toPlayerName = null;
		this.factionUUID = null;
		this.factionName = null;
		this.sentTime = null;
		this.invitationUUID = null;
	}
	
	public InvitationInfo(FactionInviteData data) {
		this.fromPlayerUUID = data.getFromPlayerUUID();
		this.fromPlayerName = data.getFromPlayerName();
		this.toPlayerUUID = data.getToPlayerUUID();
		this.toPlayerName = data.getToPlayerName();
		this.factionUUID = data.getFactionUUID();
		this.factionName = FactionDataManager.get().getFaction(data.getFactionUUID())
		                   .map(FactionData::getFactionName)
		                   .orElse("Unknown");
		this.sentTime = data.getSentTime();
		this.invitationUUID = data.getInvitationUUID();
	}
	
	private InvitationInfo(UUID fromPlayerUUID, String fromPlayerName, UUID toPlayerUUID, String toPlayerName, UUID factionUUID, String factionName, SerializableDateTime sentTime, UUID invitationUUID) {
		this.fromPlayerUUID = fromPlayerUUID;
		this.fromPlayerName = fromPlayerName;
		this.toPlayerUUID = toPlayerUUID;
		this.toPlayerName = toPlayerName;
		this.factionUUID = factionUUID;
		this.factionName = factionName;
		this.sentTime = sentTime;
		this.invitationUUID = invitationUUID;
	}
	
	public UUID getFromPlayerUUID() {
		return fromPlayerUUID;
	}
	
	public String getFromPlayerName() {
		return fromPlayerName;
	}
	
	public UUID getToPlayerUUID() {
		return toPlayerUUID;
	}
	
	public String getToPlayerName() {
		return toPlayerName;
	}
	
	public UUID getFactionUUID() {
		return factionUUID;
	}
	
	public String getFactionName() {
		return factionName;
	}
	
	public SerializableDateTime getSentTime() {
		return sentTime;
	}
	
	public UUID getInvitationUUID() {
		return invitationUUID;
	}
	
	@Override
	public String toString() {
		return "InvitationInfo{" +
		       "fromPlayerUUID=" + fromPlayerUUID +
		       ", fromPlayerName='" + fromPlayerName + '\'' +
		       ", toPlayerUUID=" + toPlayerUUID +
		       ", toPlayerName='" + toPlayerName + '\'' +
		       ", factionUUID=" + factionUUID +
		       ", sentTime=" + sentTime +
		       ", invitationUUID=" + invitationUUID +
		       '}';
	}
	
	@Override
	public void toBytes(FriendlyByteBuf byteBuf) {
		byteBuf.writeUUID(this.fromPlayerUUID);
		byteBuf.writeUtf(this.fromPlayerName);
		byteBuf.writeUUID(this.toPlayerUUID);
		byteBuf.writeUtf(this.toPlayerName);
		byteBuf.writeUUID(this.factionUUID);
		byteBuf.writeUtf(this.factionName);
		this.sentTime.toBytes(byteBuf);
		byteBuf.writeUUID(this.invitationUUID);
	}
	
	@Override
	public InvitationInfo fromBytes(FriendlyByteBuf byteBuf) {
		return new InvitationInfo(
		byteBuf.readUUID(),
		byteBuf.readUtf(),
		byteBuf.readUUID(),
		byteBuf.readUtf(),
		byteBuf.readUUID(),
		byteBuf.readUtf(),
		new SerializableDateTime().fromBytes(byteBuf),
		byteBuf.readUUID()
		);
	}
}
