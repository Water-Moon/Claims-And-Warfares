package net.rainfantasy.claims_and_warfares.common.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor.Brightness;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.Random;

public class ColorUtil {
	
	public static int alpha(int color) {
		return color >> 24 & 255;
	}
	
	public static int blue(int color) {
		return color & 255;
	}
	
	public static int green(int color) {
		return color >> 8 & 255;
	}
	
	public static int red(int color) {
		return color >> 16 & 255;
	}
	
	public static int combine(int alpha, int red, int green, int blue) {
		return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
	}
	
	public static int getColor(Block block) {
		int color = block.defaultMapColor().col;
		return combine(255, red(color), green(color), blue(color));
	}
	
	public static int getColor(Level level, BlockState block, BlockPos pos) {
		int color = block.getMapColor(level, pos).calculateRGBColor(Brightness.NORMAL);
		color = combine(255, blue(color), green(color), red(color));
		int height1 = getTopBlockHeight(level, pos.getX() - 1, pos.getZ());
		int height2 = getTopBlockHeight(level, pos.getX(), pos.getZ() - 1);
		float heightDiff = pos.getY() - ((height1 + height2) / 2f);   //positive if higher, negative if lower
		double factor = Mth.clamp(heightDiff / 16, -0.5, 0.5);
		if (factor >= 0) {
			color = lightenColor(color, (int) (factor * 255));
		} else {
			color = darkenColor(color, (int) (-factor * 255));
		}
		
		if (block.getBlock() instanceof LiquidBlock) {
			Pair<BlockState, BlockPos> data = getNonLiquidBlock(level, pos.getX(), pos.getZ());
			int color2 = data.getLeft().getMapColor(level, data.getRight()).calculateRGBColor(Brightness.NORMAL);
			color2 = combine(255, blue(color2), green(color2), red(color2));
			int height21 = getTopNonLiquidBlockHeight(level, pos.getX() - 1, pos.getZ());
			int height22 = getTopNonLiquidBlockHeight(level, pos.getX(), pos.getZ() - 1);
			float heightDiff2 = pos.getY() - ((height21 + height22) / 2f);   //positive if higher, negative if lower
			double factor2 = Mth.clamp(heightDiff2 / 8, -0.5, 0.5);
			if (factor2 >= 0) {
				color2 = lightenColor(color2, (int) (factor2 * 255));
			} else {
				color2 = darkenColor(color2, (int) (-factor2 * 255));
			}
			int depthDiff = pos.getY() - data.getRight().getY();
			int alpha = Mth.clamp((depthDiff + 32) * 4, 32, 224);
			color = overlayColor(color2, combine(alpha, red(color), green(color), blue(color)));
		}
		
		return color;
	}
	
	
	private static Pair<BlockState, BlockPos> getNonLiquidBlock(Level level, int x, int z) {
		BlockPos pos = new BlockPos(x, level.getMaxBuildHeight(), z);
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
			(!state.isAir()) &&
			state.isSolidRender(level, pos)
			) {
				return Pair.of(state, pos);
			}
			pos = pos.below();
		}
		return Pair.of(Blocks.AIR.defaultBlockState(), pos);
	}
	
	private static int getTopNonLiquidBlockHeight(Level level, int x, int z) {
		BlockPos pos = new BlockPos(x, level.getMaxBuildHeight(), z);
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
			(!state.isAir()) &&
			state.isSolidRender(level, pos)
			) {
				return pos.getY();
			}
			pos = pos.below();
		}
		return pos.getY();
	}
	
	private static int getTopBlockHeight(Level level, int x, int z) {
		BlockPos pos = new BlockPos(x, level.getMaxBuildHeight(), z);
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
			(!state.isAir()) //&&
				//state.isSolidRender(level, pos)
			) {
				return pos.getY();
			}
			pos = pos.below();
		}
		return pos.getY();
	}
	
	public static int averageColor(int... colors) {
		int a = 0;
		int r = 0;
		int g = 0;
		int b = 0;
		
		for (int color : colors) {
			a += color >> 24 & 255;
			r += color >> 16 & 255;
			g += color >> 8 & 255;
			b += color & 255;
		}
		
		return a / colors.length << 24 | r / colors.length << 16 | g / colors.length << 8 | b / colors.length;
	}
	
	public static int overlayColor(int baseRGBA, int overlayRGBA) {
		int a = overlayRGBA >> 24 & 255;
		int r = overlayRGBA >> 16 & 255;
		int g = overlayRGBA >> 8 & 255;
		int b = overlayRGBA & 255;
		
		int baseR = baseRGBA >> 16 & 255;
		int baseG = baseRGBA >> 8 & 255;
		int baseB = baseRGBA & 255;
		
		int newA = 255;
		int newR = r * a / 255 + baseR * (255 - a) / 255;
		int newG = g * a / 255 + baseG * (255 - a) / 255;
		int newB = b * a / 255 + baseB * (255 - a) / 255;
		
		return newA << 24 | newR << 16 | newG << 8 | newB;
	}
	
	public static int lightenColor(int color, int amount) {
		int a = color >> 24 & 255;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		
		r = Mth.clamp(r + amount, 0, 255);
		g = Mth.clamp(g + amount, 0, 255);
		b = Mth.clamp(b + amount, 0, 255);
		
		return a << 24 | r << 16 | g << 8 | b;
	}
	
	public static int darkenColor(int color, int amount) {
		int a = color >> 24 & 255;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		
		r = Mth.clamp(r - amount, 0, 255);
		g = Mth.clamp(g - amount, 0, 255);
		b = Mth.clamp(b - amount, 0, 255);
		
		return a << 24 | r << 16 | g << 8 | b;
	}
	
	@Contract(pure = true)
	public static int randomMinecraftColor() {
		int v = new Random(System.currentTimeMillis()).nextInt(0, 16);
		int color = Optional.ofNullable(ChatFormatting.getById(v)).map(ChatFormatting::getColor).orElse(16777215);
		return combine(255, red(color), green(color), blue(color));
	}
	
	public static float[] toFloatColor(int color) {
		return new float[]{red(color) / 255f, green(color) / 255f, blue(color) / 255f};
	}
}
