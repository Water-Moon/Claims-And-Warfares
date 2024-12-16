package net.rainfantasy.claims_and_warfares.client.widgets.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rainfantasy.claims_and_warfares.client.CAWClientGUIManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimedChunkInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.rainfantasy.claims_and_warfares.common.utils.ColorUtil.*;

public class MapTexture extends AbstractMapTexture {
	
	int[][] colors;
	ClaimedChunkInfo[][] claimInfos;
	
	public MapTexture() {
		this.updateArraySize();
		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight; j++) {
				this.colors[i][j] = combine(255, 0, 0, 0);
			}
		}
	}
	
	private void updateArraySize() {
		if(CAWClientGUIManager.mapInfo == null || CAWClientGUIManager.mapInfo.length == 0 || CAWClientGUIManager.mapInfo[0].length == 0) {
			return;
		}
		this.imageWidth = CAWClientGUIManager.mapInfo.length;
		this.imageHeight = CAWClientGUIManager.mapInfo[0].length;
		if (this.colors == null || this.colors.length != imageWidth || this.colors[0].length != imageHeight) {
			this.colors = new int[imageWidth][imageHeight];
		}
		if (this.claimInfos == null || CAWClientGUIManager.claimInfoDirty || this.claimInfos.length != CAWClientGUIManager.claimedChunkInfo.length || this.claimInfos[0].length != CAWClientGUIManager.claimedChunkInfo.length) {
			this.claimInfos = new ClaimedChunkInfo[imageWidth >> 4][imageHeight >> 4];
		}
	}
	
	@Override
	NativeImage getImage() {
		NativeImage image = new NativeImage(imageWidth, imageHeight, true);
		
		for (int i = 0; i < imageWidth; ++i) {
			for (int j = 0; j < imageHeight; ++j) {
				int color = colors[i][j];
				if (i % 16 == 0 || j % 16 == 0) {
					color = averageColor(color, combine(255, 0, 0, 0));
				}
				int chunkX = i >> 4;
				int chunkZ = j >> 4;
				if (CAWClientGUIManager.claimedChunkInfo[chunkX][chunkZ] != null) {
					int claimColor = CAWClientGUIManager.claimedChunkInfo[chunkX][chunkZ].color;
					color = overlayColor(color, combine(128, red(claimColor), green(claimColor), blue(claimColor)));
				}
				int colorReversed = combine(alpha(color), blue(color), green(color), red(color));
				image.setPixelRGBA(i, j, colorReversed);
			}
		}
		
		return image;
	}
	
	public void update() {
		this.updateArraySize();
		for (int i = 0; i < imageWidth; i++) {
			System.arraycopy(CAWClientGUIManager.mapInfo[i], 0, this.colors[i], 0, imageHeight);
		}
		for (int i = 0; i < claimInfos.length; i++) {
			System.arraycopy(CAWClientGUIManager.claimedChunkInfo[i], 0, this.claimInfos[i], 0, claimInfos[i].length);
		}
		this.load();
	}
	
	@Override
	public void load(@NotNull ResourceManager resourceManager) throws IOException {
		this.update();
		this.load();
	}
}
