package net.rainfantasy.claims_and_warfares.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.faction.*;
import net.rainfantasy.claims_and_warfares.client.map.ClaimMapScreen;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimedChunkInfo;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCClaimInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCMapInfoPacket;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.PTSOpenFactionManagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class CAWClientGUIManager {
	
	public static int[][] mapInfo = new int[0][0];
	public static ClaimedChunkInfo[][] claimedChunkInfo = new ClaimedChunkInfo[0][0];
	public static boolean claimInfoDirty = false;
	public static Vector2i mapPos = new Vector2i(0, 0);
	private static Component lastMessage = null;
	
	public static void clearLastMessage() {
		lastMessage = null;
	}
	
	public static Optional<Component> getLastMessage() {
		return Optional.ofNullable(lastMessage);
	}
	
	public static void setLastMessage(Component message) {
		lastMessage = message;
	}
	
	public static void openClaimMap(int radiusX, int radiusZ) {
		setClaimInfoSize(radiusX, radiusZ);
		Minecraft.getInstance().execute(() -> {
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new ClaimMapScreen());
			}
		});
	}
	
	public static void setClaimInfoSize(int radiusX, int radiusZ){
		int sizeX = 16 * (radiusX * 2 + 1);
		int sizeZ = 16 * (radiusZ * 2 + 1);
		mapInfo = new int[sizeX][sizeZ];
		claimedChunkInfo = new ClaimedChunkInfo[sizeX >> 4][sizeZ >> 4];
		for(int i = 0; i < mapInfo.length; i++){
			for(int j = 0; j < mapInfo[i].length; j++){
				boolean flag = (i / 16) % 2 == 0;
				boolean flag1 = (j / 16) % 2 == 0;
				if(flag == flag1){
					mapInfo[i][j] = ColorUtil.combine(255, 0, 0, 0);
				}else{
					mapInfo[i][j] = ColorUtil.combine(255, 255, 0, 255);
				}
			}
		}
	}
	
	public static void openFactionManagementPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new FactionManagementScreen());
			}
		});
	}
	
	public static void openFactionCreationPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new FactionCreationScreen());
			}
		});
	}
	
	public static void openFactionInvitePage(){
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new InvitationScreen());
			}
		});
	}
	
	public static void openFactionSelectPage(){
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new SelectCurrentActiveFactionScreen());
			}
		});
	}
	
	public static void openViewInvitationPage(){
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new ViewInvitationScreen());
			}
		});
	}
	
	public static boolean validateMapInfo(PTCMapInfoPacket packet) {
		return packet.NorthernX >= 0 && packet.EasternZ >= 0 && packet.NorthernX + packet.data.length <= mapInfo.length && packet.EasternZ + packet.data[0].length <= mapInfo[0].length;
	}
	
	public static void updateMapInfo(PTCMapInfoPacket packet) {
		if (!validateMapInfo(packet)) {
			CAWConstants.LOGGER.warn("Invalid MapInfo Packet{}", packet);
			return;
		}
		Minecraft.getInstance().executeIfPossible(() -> {
			int NorthernX = packet.NorthernX;
			int EasternZ = packet.EasternZ;
			//noinspection SuspiciousNameCombination
			mapPos = new Vector2i(packet.TopX, packet.LeftZ);
			for (int i = 0; i < packet.data.length; i++) {
				System.arraycopy(packet.data[i], 0, mapInfo[NorthernX + i], EasternZ, packet.data[i].length);
			}
		});
	}
	
	public static void updateClaimInfo(PTCClaimInfoPacket packet) {
		Minecraft.getInstance().executeIfPossible(() -> {
			int posX = packet.offsetX;
			int posZ = packet.offsetZ;
			claimedChunkInfo[posX][posZ] = packet.info;
			claimInfoDirty = true;
		});
	}
	
	public static void factionCreateSuccess(String factionName) {
		if (Minecraft.getInstance().player != null) {
			displayMessage(Component.translatable("caw.message.faction.create_success", Component.literal(factionName)));
			if (Minecraft.getInstance().screen instanceof FactionCreationScreen) {
				ChannelRegistry.sendToServer(new PTSOpenFactionManagePacket());
			}
		}
	}
	
	public static void displayMessage(Component message) {
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.sendSystemMessage(message);
		}
	}
	
	public static void openLeaveFactionPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new LeaveFactionConfirmScreen());
			}
		});
	}
	
	public static void openTransferFactionConfirmPage(UUID factionUUID, UUID newOwnerUUID) {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(TransferFactionConfirmScreen.MakeTransferFactionScreen(factionUUID, newOwnerUUID));
			}
		});
		
	}
	
	public static void openMemberManageScreen() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new MemberManagementScreen());
			}
		});
	}
	
	public static void openTransferFactionPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new TransferFactionScreen());
			}
		});
	}
	
	public static void openDisbandConfirmScreen() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new DisbandConfirmScreen());
			}
		});
	}
	
	public static void openRelationshipPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new RelationshipManagementScreen());
			}
		});
	}
	
	public static void openFactionSettingsPage() {
		Minecraft.getInstance().execute(() -> {
			clearLastMessage();
			if (Minecraft.getInstance().level != null) {
				Minecraft.getInstance().setScreen(new FactionSettingsScreen());
			}
		});
	}
}
