package net.rainfantasy.claims_and_warfares.common.setups.networking.claim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Supplier;

public class PTCMapInfoPacket {
	
	public int NorthernX;
	public int EasternZ;
	public int TopX;
	public int LeftZ;
	public int[][] data;
	
	/**
	 * @param NorthernX X Coordinate of the upper left corner of the map (0 for the upper left corner of the map)
	 * @param EasternZ  Z Coordinate of the upper left corner of the map (0 for the upper left corner of the map)
	 * @param TopX      X Coordinate of the top left corner of the map in world
	 * @param LeftZ     Z Coordinate of the top left corner of the map in world
	 * @param data      2D array of color data
	 */
	public PTCMapInfoPacket(int NorthernX, int EasternZ, int TopX, int LeftZ, int[][] data) {
		this.NorthernX = NorthernX;
		this.EasternZ = EasternZ;
		this.TopX = TopX;
		this.LeftZ = LeftZ;
		this.data = data;
	}
	
	public static @NotNull PTCMapInfoPacket fromBytes(FriendlyByteBuf byteBuf) {
		int upperX = byteBuf.readVarInt();
		int leftZ = byteBuf.readVarInt();
		int worldX = byteBuf.readVarInt();
		int worldZ = byteBuf.readVarInt();
		int[][] data = new int[byteBuf.readVarInt()][];
		for (int i = 0; i < data.length; i++) {
			data[i] = new int[byteBuf.readVarInt()];
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = byteBuf.readVarInt();
			}
		}
		return new PTCMapInfoPacket(upperX, leftZ, worldX, worldZ, data);
	}
	
	public static void toBytes(PTCMapInfoPacket packet, FriendlyByteBuf byteBuf) {
		byteBuf.writeVarInt(packet.NorthernX);
		byteBuf.writeVarInt(packet.EasternZ);
		byteBuf.writeVarInt(packet.TopX);
		byteBuf.writeVarInt(packet.LeftZ);
		byteBuf.writeVarInt(packet.data.length);
		for (int i = 0; i < packet.data.length; i++) {
			byteBuf.writeVarInt(packet.data[i].length);
			for (int j = 0; j < packet.data[i].length; j++) {
				byteBuf.writeVarInt(packet.data[i][j]);
			}
		}
	}
	
	public void execute(@NotNull Supplier<Context> supplier) {
		Context context = supplier.get();
		CAWConstants.execute(() -> {
			CAWClientGUIManager.updateMapInfo(this);
		});
		context.setPacketHandled(true);
	}
	
	@Override
	public String toString() {
		return "PTCMapInfoPacket{" +
		       "NorthernX=" + NorthernX +
		       ", EasternZ=" + EasternZ +
		       ", data=" + Arrays.toString(data) +
		       '}';
	}
}
