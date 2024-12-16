package net.rainfantasy.claims_and_warfares.common.setups.registries;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericErrorPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCGenericMessagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.PTCUpdateClientUUIDPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCClaimInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCMapInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCMapResizePacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCOpenViewerPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconHackerOnOffPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconHackerSelectTargetPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim_beacon.PTSBeaconInstructionPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.*;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.*;

import java.util.Optional;

public class ChannelRegistry {
	
	public static SimpleChannel CAW_CHANNEL;
	private static int i = 0;
	public static final String VERSION = "1.0.0";
	
	public static boolean isSameVersion(String other) {
		return VERSION.equals(other);
	}
	
	
	public static void setup() {
		CAW_CHANNEL = NetworkRegistry.newSimpleChannel(CAWConstants.rl("default"),
		() -> VERSION,
		ChannelRegistry::isSameVersion,
		ChannelRegistry::isSameVersion);
		
		///Claims
		
		CAW_CHANNEL.registerMessage(i++, PTCClaimInfoPacket.class,
		PTCClaimInfoPacket::toBytes, PTCClaimInfoPacket::fromBytes,
		PTCClaimInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCMapInfoPacket.class,
		PTCMapInfoPacket::toBytes, PTCMapInfoPacket::fromBytes,
		PTCMapInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCMapResizePacket.class,
		PTCMapResizePacket::toBytes, PTCMapResizePacket::fromBytes,
		PTCMapResizePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenViewerPacket.class,
		PTCOpenViewerPacket::toBytes, PTCOpenViewerPacket::fromBytes,
		PTCOpenViewerPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		///Open Menu
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenDisbandFactionPacket.class,
		PTCOpenDisbandFactionPacket::toBytes, PTCOpenDisbandFactionPacket::fromBytes,
		PTCOpenDisbandFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenDisbandFactionPacket.class,
		PTSOpenDisbandFactionPacket::toBytes, PTSOpenDisbandFactionPacket::fromBytes,
		PTSOpenDisbandFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenFactionCreatePacket.class,
		PTCOpenFactionCreatePacket::toBytes, PTCOpenFactionCreatePacket::fromBytes,
		PTCOpenFactionCreatePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenFactionCreatePacket.class,
		PTSOpenFactionCreatePacket::toBytes, PTSOpenFactionCreatePacket::fromBytes,
		PTSOpenFactionCreatePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenFactionInvitePacket.class,
		PTCOpenFactionInvitePacket::toBytes, PTCOpenFactionInvitePacket::fromBytes,
		PTCOpenFactionInvitePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenFactionInvitePacket.class,
		PTSOpenFactionInvitePacket::toBytes, PTSOpenFactionInvitePacket::fromBytes,
		PTSOpenFactionInvitePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenFactionManagePacket.class,
		PTCOpenFactionManagePacket::toBytes, PTCOpenFactionManagePacket::fromBytes,
		PTCOpenFactionManagePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenFactionManagePacket.class,
		PTSOpenFactionManagePacket::toBytes, PTSOpenFactionManagePacket::fromBytes,
		PTSOpenFactionManagePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenFactionSettingPacket.class,
		PTCOpenFactionSettingPacket::toBytes, PTCOpenFactionSettingPacket::fromBytes,
		PTCOpenFactionSettingPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenFactionSettingPacket.class,
		PTSOpenFactionSettingPacket::toBytes, PTSOpenFactionSettingPacket::fromBytes,
		PTSOpenFactionSettingPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenLeaveFactionPacket.class,
		PTCOpenLeaveFactionPacket::toBytes, PTCOpenLeaveFactionPacket::fromBytes,
		PTCOpenLeaveFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenLeaveFactionPacket.class,
		PTSOpenLeaveFactionPacket::toBytes, PTSOpenLeaveFactionPacket::fromBytes,
		PTSOpenLeaveFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenMemberManagePacket.class,
		PTCOpenMemberManagePacket::toBytes, PTCOpenMemberManagePacket::fromBytes,
		PTCOpenMemberManagePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenMemberManagePacket.class,
		PTSOpenMemberManagePacket::toBytes, PTSOpenMemberManagePacket::fromBytes,
		PTSOpenMemberManagePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenRelationshipPacket.class,
		PTCOpenRelationshipPacket::toBytes, PTCOpenRelationshipPacket::fromBytes,
		PTCOpenRelationshipPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenRelationshipPacket.class,
		PTSOpenRelationshipPacket::toBytes, PTSOpenRelationshipPacket::fromBytes,
		PTSOpenRelationshipPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenSelectActiveFactionPacket.class,
		PTCOpenSelectActiveFactionPacket::toBytes, PTCOpenSelectActiveFactionPacket::fromBytes,
		PTCOpenSelectActiveFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenSelectActiveFactionPacket.class,
		PTSOpenSelectActiveFactionPacket::toBytes, PTSOpenSelectActiveFactionPacket::fromBytes,
		PTSOpenSelectActiveFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenTransferFactionConfirmPacket.class,
		PTCOpenTransferFactionConfirmPacket::toBytes, PTCOpenTransferFactionConfirmPacket::fromBytes,
		PTCOpenTransferFactionConfirmPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenTransferFactionConfirmPacket.class,
		PTSOpenTransferFactionConfirmPacket::toBytes, PTSOpenTransferFactionConfirmPacket::fromBytes,
		PTSOpenTransferFactionConfirmPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenTransferFactionPacket.class,
		PTCOpenTransferFactionPacket::toBytes, PTCOpenTransferFactionPacket::fromBytes,
		PTCOpenTransferFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenTransferFactionPacket.class,
		PTSOpenTransferFactionPacket::toBytes, PTSOpenTransferFactionPacket::fromBytes,
		PTSOpenTransferFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTCOpenViewInvitationPacket.class,
		PTCOpenViewInvitationPacket::toBytes, PTCOpenViewInvitationPacket::fromBytes,
		PTCOpenViewInvitationPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSOpenViewInvitationPacket.class,
		PTSOpenViewInvitationPacket::toBytes, PTSOpenViewInvitationPacket::fromBytes,
		PTSOpenViewInvitationPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		///Factions
		
		CAW_CHANNEL.registerMessage(i++, PTCFactionCreatedPacket.class,
		PTCFactionCreatedPacket::toBytes, PTCFactionCreatedPacket::fromBytes,
		PTCFactionCreatedPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCAcceptInvitationSuccessPacket.class,
		PTCAcceptInvitationSuccessPacket::toBytes, PTCAcceptInvitationSuccessPacket::fromBytes,
		PTCAcceptInvitationSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCDemoteAdminSuccessPacket.class,
		PTCDemoteAdminSuccessPacket::toBytes, PTCDemoteAdminSuccessPacket::fromBytes,
		PTCDemoteAdminSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCDiplomaticRelationshipData.class,
		PTCDiplomaticRelationshipData::toBytes, PTCDiplomaticRelationshipData::fromBytes,
		PTCDiplomaticRelationshipData::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCDisbandSuccessPacket.class,
		PTCDisbandSuccessPacket::toBytes, PTCDisbandSuccessPacket::fromBytes,
		PTCDisbandSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCFactionInfoPacket.class,
		PTCFactionInfoPacket::toBytes, PTCFactionInfoPacket::fromBytes,
		PTCFactionInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCInvitationCancelSuccessPacket.class,
		PTCInvitationCancelSuccessPacket::toBytes, PTCInvitationCancelSuccessPacket::fromBytes,
		PTCInvitationCancelSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCInvitationInfoPacket.class,
		PTCInvitationInfoPacket::toBytes, PTCInvitationInfoPacket::fromBytes,
		PTCInvitationInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCInvitationSuccessPacket.class,
		PTCInvitationSuccessPacket::toBytes, PTCInvitationSuccessPacket::fromBytes,
		PTCInvitationSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCKickSuccessPacket.class,
		PTCKickSuccessPacket::toBytes, PTCKickSuccessPacket::fromBytes,
		PTCKickSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCLeaveFactionSuccessPacket.class,
		PTCLeaveFactionSuccessPacket::toBytes, PTCLeaveFactionSuccessPacket::fromBytes,
		PTCLeaveFactionSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCMakeAdminSuccessPacket.class,
		PTCMakeAdminSuccessPacket::toBytes, PTCMakeAdminSuccessPacket::fromBytes,
		PTCMakeAdminSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCOfflinePlayerInfoPacket.class,
		PTCOfflinePlayerInfoPacket::toBytes, PTCOfflinePlayerInfoPacket::fromBytes,
		PTCOfflinePlayerInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCRejectInvitationSuccessPacket.class,
		PTCRejectInvitationSuccessPacket::toBytes, PTCRejectInvitationSuccessPacket::fromBytes,
		PTCRejectInvitationSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCRequestClientRefreshPacket.class,
		PTCRequestClientRefreshPacket::toBytes, PTCRequestClientRefreshPacket::fromBytes,
		PTCRequestClientRefreshPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCSetRelationshipSuccessPacket.class,
		PTCSetRelationshipSuccessPacket::toBytes, PTCSetRelationshipSuccessPacket::fromBytes,
		PTCSetRelationshipSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCServerPlayerInfoPacket.class,
		PTCServerPlayerInfoPacket::toBytes, PTCServerPlayerInfoPacket::fromBytes,
		PTCServerPlayerInfoPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCTransferFactionSuccessPacket.class,
		PTCTransferFactionSuccessPacket::toBytes, PTCTransferFactionSuccessPacket::fromBytes,
		PTCTransferFactionSuccessPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCUpdateCurrentSelectedFactionPacket.class,
		PTCUpdateCurrentSelectedFactionPacket::toBytes, PTCUpdateCurrentSelectedFactionPacket::fromBytes,
		PTCUpdateCurrentSelectedFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTSAcceptInvitationPacket.class,
		PTSAcceptInvitationPacket::toBytes, PTSAcceptInvitationPacket::fromBytes,
		PTSAcceptInvitationPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSCancelInvitePacket.class,
		PTSCancelInvitePacket::toBytes, PTSCancelInvitePacket::fromBytes,
		PTSCancelInvitePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSCreateFactionPacket.class,
		PTSCreateFactionPacket::toBytes, PTSCreateFactionPacket::fromBytes,
		PTSCreateFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSDemoteAdminPacket.class,
		PTSDemoteAdminPacket::toBytes, PTSDemoteAdminPacket::fromBytes,
		PTSDemoteAdminPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSDisbandFactionPacket.class,
		PTSDisbandFactionPacket::toBytes, PTSDisbandFactionPacket::fromBytes,
		PTSDisbandFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSInvitePlayerPacket.class,
		PTSInvitePlayerPacket::toBytes, PTSInvitePlayerPacket::fromBytes,
		PTSInvitePlayerPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSKickMemberPacket.class,
		PTSKickMemberPacket::toBytes, PTSKickMemberPacket::fromBytes,
		PTSKickMemberPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSLeaveFactionPacket.class,
		PTSLeaveFactionPacket::toBytes, PTSLeaveFactionPacket::fromBytes,
		PTSLeaveFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSMakeAdminPacket.class,
		PTSMakeAdminPacket::toBytes, PTSMakeAdminPacket::fromBytes,
		PTSMakeAdminPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSRejectInvitationPacket.class,
		PTSRejectInvitationPacket::toBytes, PTSRejectInvitationPacket::fromBytes,
		PTSRejectInvitationPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSRenamePacket.class,
		PTSRenamePacket::toBytes, PTSRenamePacket::fromBytes,
		PTSRenamePacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSSetColorPacket.class,
		PTSSetColorPacket::toBytes, PTSSetColorPacket::fromBytes,
		PTSSetColorPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSSetRelationshipPacket.class,
		PTSSetRelationshipPacket::toBytes, PTSSetRelationshipPacket::fromBytes,
		PTSSetRelationshipPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSSetFakePlayerPolicyPacket.class,
		PTSSetFakePlayerPolicyPacket::toBytes, PTSSetFakePlayerPolicyPacket::fromBytes,
		PTSSetFakePlayerPolicyPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSSetPrimaryFactionPacket.class,
		PTSSetPrimaryFactionPacket::toBytes, PTSSetPrimaryFactionPacket::fromBytes,
		PTSSetPrimaryFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSTransferFactionPacket.class,
		PTSTransferFactionPacket::toBytes, PTSTransferFactionPacket::fromBytes,
		PTSTransferFactionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		///Claim Beacon
		
		CAW_CHANNEL.registerMessage(i++, PTSBeaconInstructionPacket.class,
		PTSBeaconInstructionPacket::toBytes, PTSBeaconInstructionPacket::fromBytes,
		PTSBeaconInstructionPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSBeaconHackerOnOffPacket.class,
		PTSBeaconHackerOnOffPacket::toBytes, PTSBeaconHackerOnOffPacket::fromBytes,
		PTSBeaconHackerOnOffPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		CAW_CHANNEL.registerMessage(i++, PTSBeaconHackerSelectTargetPacket.class,
		PTSBeaconHackerSelectTargetPacket::toBytes, PTSBeaconHackerSelectTargetPacket::fromBytes,
		PTSBeaconHackerSelectTargetPacket::execute, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		
		///Generic
		
		CAW_CHANNEL.registerMessage(i++, PTCGenericMessagePacket.class,
		PTCGenericMessagePacket::toBytes, PTCGenericMessagePacket::fromBytes,
		PTCGenericMessagePacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCGenericErrorPacket.class,
		PTCGenericErrorPacket::toBytes, PTCGenericErrorPacket::fromBytes,
		PTCGenericErrorPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
		CAW_CHANNEL.registerMessage(i++, PTCUpdateClientUUIDPacket.class,
		PTCUpdateClientUUIDPacket::toBytes, PTCUpdateClientUUIDPacket::fromBytes,
		PTCUpdateClientUUIDPacket::execute, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		
	}
	
	public static void sendToClient(ServerPlayer player, Object message) {
		CAW_CHANNEL.sendTo(
		message,
		player.connection.connection,
		NetworkDirection.PLAY_TO_CLIENT
		);
	}
	
	public static void sendToServer(Object message) {
		CAW_CHANNEL.sendToServer(message);
	}
	
	public static void reply(Context context, Object message) {
		CAW_CHANNEL.reply(message, context);
	}
	
	public static void sendErrorToClient(ServerPlayer player, String translationKey) {
		sendErrorToClient(player, Component.translatable(translationKey));
	}
	
	public static void sendErrorToClient(ServerPlayer player, Component message) {
		PTCGenericErrorPacket packet = new PTCGenericErrorPacket(message);
		sendToClient(player, packet);
	}
}
