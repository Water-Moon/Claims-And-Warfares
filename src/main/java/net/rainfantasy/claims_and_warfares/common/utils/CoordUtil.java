package net.rainfantasy.claims_and_warfares.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CoordUtil {
	
	public static Vector2i blockToChunk(Vector2i coord) {
		return new Vector2i(coord.x >> 4, coord.y >> 4);
	}
	
	public static Vector2i blockToChunk(int x, int y) {
		return new Vector2i(x >> 4, y >> 4);
	}
	
	public static Vector2i chunkToBlock(Vector2i coord) {
		return new Vector2i(coord.x << 4, coord.y << 4);
	}
	
	public static Vector2i chunkToBlock(int x, int y) {
		return new Vector2i(x << 4, y << 4);
	}
	
	public static boolean isInChunk(Vector2i chunkCoord, Vector2i blockCoord) {
		return blockCoord.x >> 4 == chunkCoord.x && blockCoord.y >> 4 == chunkCoord.y;
	}
	
	public static List<Vector2i> getChunks(Vector2i start, Vector2i end) {
		Vector2i startChunk = blockToChunk(start);
		Vector2i endChunk = blockToChunk(end);
		
		List<Vector2i> result = new ArrayList<>();
		
		for (int x = startChunk.x; x <= endChunk.x; x++) {
			for (int y = startChunk.y; y <= endChunk.y; y++) {
				result.add(new Vector2i(x, y));
			}
		}
		
		return result;
	}
	
	public static Vector2i blockToChunk(BlockPos pos) {
		return blockToChunk(new Vector2i(pos.getX(), pos.getZ()));
	}
	
	public static Vector2i blockToChunk(Vec3 pos) {
		return blockToChunk(new Vector2i((int) pos.x, (int) pos.z));
	}
	
	public static Stream<Vector2i> iterateCoords(Vector2i min, Vector2i max) {
		if(min.x > max.x){
			int temp = min.x;
			min.x = max.x;
			max.x = temp;
		}
		if(min.y > max.y){
			int temp = min.y;
			min.y = max.y;
			max.y = temp;
		}
		return Stream.iterate(min, v -> new Vector2i(v.x() + 1, v.y()))
		       .limit(max.x() - min.x() + 1)
		       .flatMap(x -> Stream.iterate(x, v -> new Vector2i(v.x(), v.y() + 1))
		                     .limit(max.y() - min.y() + 1));
	}
	
	public static Pair<BlockState, BlockPos> findTopBlockAt(Level level, BlockPos posIn, boolean solidOnly){
		BlockPos pos = new BlockPos(posIn.getX(), level.getMaxBuildHeight(), posIn.getZ());
		while (pos.getY() > level.getMinBuildHeight()) {
			BlockState state = level.getBlockState(pos);
			if (
				(!state.isAir()) &&
				((!solidOnly) || state.isSolidRender(level, pos))
			) {
				return Pair.of(state, pos);
			}
			pos = pos.below();
		}
		return Pair.of(Blocks.AIR.defaultBlockState(), pos);
	}
	
	public static int getTopBlockHeight(Level level, BlockPos posIn, boolean solidOnly){
		return findTopBlockAt(level, posIn, solidOnly).getRight().getY();
	}
	
}
