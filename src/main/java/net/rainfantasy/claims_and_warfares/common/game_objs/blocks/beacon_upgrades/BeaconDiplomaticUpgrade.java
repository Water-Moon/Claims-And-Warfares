package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_block_entities.BeaconDiplomaticUpgradeBE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BeaconDiplomaticUpgrade extends BaseAdvancedBeaconUpgradeBlock implements IBeaconUpgrade {
	
	@Override
	public void apply(BeaconUpgradeLoader loader, Level level, BlockPos pos) {
		if(level.isClientSide) return;
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BeaconDiplomaticUpgradeBE upgradeBE) {
			upgradeBE.apply(loader);
		} else {
			throw new IllegalStateException("Block entity missing for " + level.getBlockState(pos) + " at " + pos);
		}
	}
	
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
		if ((!pLevel.isClientSide) && (pPlayer instanceof ServerPlayer serverPlayer)) {
			BlockEntity be = pLevel.getBlockEntity(pPos);
			if (be instanceof BeaconDiplomaticUpgradeBE upgradeBE) {
				BaseAdvancedBeaconUpgradeBlock.findBeacon(pLevel, pPos).ifPresentOrElse(beacon -> {
					if (!beacon.isOwnerAndFactionValid()) {
						serverPlayer.sendSystemMessage(Component.translatable("caw.errors.beacon.not_set_properly"));
						return;
					}
					if (!FactionDataManager.get().isPermissionEqualOrHigher(beacon.getOwnerUUID(), serverPlayer.getUUID(), beacon.getOwningFactionUUID())) {
						serverPlayer.sendSystemMessage(Component.translatable("caw.errors.beacon.lower_permission", beacon.getOwnerName(), beacon.getOwningFactionName()));
						return;
					}
					upgradeBE.openScreen(serverPlayer);
				}, () -> {
					serverPlayer.sendSystemMessage(Component.translatable("caw.errors.beacon.beacon_not_found"));
				});
			} else {
				throw new IllegalStateException("Block entity missing for " + pState + " at " + pPos);
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}
	
	@Override
	public String getId() {
		return "diplomatic_upgrade";
	}
	
	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new BeaconDiplomaticUpgradeBE(pPos, pState);
	}
	
	@Override
	public @NotNull RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
}
