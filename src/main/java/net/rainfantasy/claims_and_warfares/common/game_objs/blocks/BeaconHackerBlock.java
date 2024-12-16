package net.rainfantasy.claims_and_warfares.common.game_objs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.mapper.MapPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockEntityRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BeaconHackerBlock extends BaseEntityBlock {
	
	public BeaconHackerBlock() {
		super(Properties.copy(Blocks.SMITHING_TABLE));
	}
	
	@Override
	public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
		super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
	}
	
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
		if ((!pLevel.isClientSide) && (pPlayer instanceof ServerPlayer serverPlayer)) {
			BlockEntity be = pLevel.getBlockEntity(pPos);
			if (be instanceof BeaconHackerBlockEntity beaconHackerBlockEntity) {
				MapPacketGenerator.scheduleSend(serverPlayer, CoordUtil.blockToChunk(pPos), 2, 2);
				ClaimPacketGenerator.scheduleSend(serverPlayer, CoordUtil.blockToChunk(pPos), 2, 2);
				FactionPacketGenerator.scheduleSend(serverPlayer);
				beaconHackerBlockEntity.openScreen(serverPlayer);
			} else {
				throw new IllegalStateException("Block entity missing for " + pState + " at " + pPos);
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}
	
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
		return new BeaconHackerBlockEntity(blockPos, blockState);
	}
	
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		if (pLevel.isClientSide) return null;
		
		return createTickerHelper(
		pBlockEntityType,
		BlockEntityRegistry.BEACON_HACKER_BE.get(),
		(((level, blockPos, blockState, block) -> block.tick(level, blockPos, blockState)))
		);
	}
	
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}
}
