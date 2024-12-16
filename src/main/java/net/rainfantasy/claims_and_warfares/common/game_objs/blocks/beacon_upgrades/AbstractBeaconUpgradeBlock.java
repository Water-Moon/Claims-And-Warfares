package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("deprecation")
public abstract class AbstractBeaconUpgradeBlock extends Block {
	
	public AbstractBeaconUpgradeBlock() {
		super(Properties.copy(Blocks.SMITHING_TABLE).explosionResistance(3600000.0F));
	}
	
	private static Optional<ClaimBeaconBlockEntity> findBeacon(Level level, BlockPos selfPos) {
		while (level.getBlockState(selfPos).getBlock() instanceof AbstractBeaconUpgradeBlock) {
			selfPos = selfPos.above();
		}
		if (level.getBlockEntity(selfPos) instanceof ClaimBeaconBlockEntity) {
			return Optional.of((ClaimBeaconBlockEntity) level.getBlockEntity(selfPos));
		}
		return Optional.empty();
	}
	
	@Override
	public float getDestroyProgress(@NotNull BlockState pState, @NotNull Player pPlayer, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
		if (pPlayer.level().isClientSide) return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
		if (!(pPlayer instanceof ServerPlayer player)) return -1f;
		Optional<ClaimBeaconBlockEntity> beacon = findBeacon((Level) pLevel, pPos);
		return beacon.filter(block -> !BeaconHelper.canBreak(player, block)).map(block -> -1f).orElseGet(() -> super.getDestroyProgress(pState, player, pLevel, pPos));
	}
	
	@Override
	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		if (entity.getCommandSenderWorld().isClientSide) return super.canEntityDestroy(state, level, pos, entity);
		if (!(entity instanceof ServerPlayer player)) return false;
		Optional<ClaimBeaconBlockEntity> beacon = findBeacon((Level) level, pos);
		return beacon.map(block -> BeaconHelper.canBreak(player, block)).orElseGet(() -> super.canEntityDestroy(state, level, pos, entity));
	}
	
	public abstract void apply(BeaconUpgradeLoader loader);
	
	public abstract String getId();
}
