package net.rainfantasy.claims_and_warfares.common.game_objs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
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
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockEntityRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconHelper;

@SuppressWarnings("deprecation")
public class ClaimBeaconBlock extends BaseEntityBlock {
	
	public ClaimBeaconBlock() {
		super(Properties.copy(Blocks.BEACON).explosionResistance(3600000.0F));
	}
	
	@Override
	public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
		//TODO drop item
		super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
	}
	
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
		if((!pLevel.isClientSide) && (pPlayer instanceof ServerPlayer serverPlayer)){
			BlockEntity be = pLevel.getBlockEntity(pPos);
			if(be instanceof ClaimBeaconBlockEntity claimBeaconBlockEntity){
				Optional<Component> errorMsg = BeaconHelper.accessUIAllowed(serverPlayer, claimBeaconBlockEntity);
				if(errorMsg.isPresent()){
					serverPlayer.sendSystemMessage(errorMsg.get());
					return InteractionResult.PASS;
				}
				
				if(!claimBeaconBlockEntity.isOwnerValid()) {
					claimBeaconBlockEntity.tryBindPlayer(serverPlayer);
				}
				if(!claimBeaconBlockEntity.isFactionValid()){
					claimBeaconBlockEntity.tryBindFaction(serverPlayer);
				}
				MapPacketGenerator.scheduleSend(serverPlayer, CoordUtil.blockToChunk(pPos), 3, 3);
				ClaimPacketGenerator.scheduleSend(serverPlayer, CoordUtil.blockToChunk(pPos), 3, 3);
				FactionPacketGenerator.scheduleSend(serverPlayer);
				claimBeaconBlockEntity.openScreen(serverPlayer);
			}else{
				throw new IllegalStateException("Block entity missing for " + pState + " at " + pPos);
			}
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}
	
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new ClaimBeaconBlockEntity(pos, state);
	}
	
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
		if(pLevel.isClientSide) return null;
		
		return createTickerHelper(
			pBlockEntityType,
			BlockEntityRegistry.CLAIM_BEACON_BE.get(),
			((level, blockPos, blockState, claimBeaconBlockEntity) -> claimBeaconBlockEntity.tick(level, blockPos, blockState))
		);
	}
	
	@Override
	public float getDestroyProgress(@NotNull BlockState pState, @NotNull Player pPlayer, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
		BlockEntity be = pLevel.getBlockEntity(pPos);
		if(be instanceof ClaimBeaconBlockEntity cbbe){
			if(!BeaconHelper.canBreak(pPlayer, cbbe)) return -1f;
		}
		return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
	}
	
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Override
	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be instanceof ClaimBeaconBlockEntity cbbe){
			if(!(entity instanceof ServerPlayer player)) return false;
			return BeaconHelper.canBreak(player, cbbe);
		}
		return super.canEntityDestroy(state, level, pos, entity);
	}
}
/*
 * 正常状态：关闭、启动中、运行中、关闭中
 * 被攻击后进入不稳定状态（与运行中效果相同，但不能关闭）
 * 不稳定状态在国战开始时会进入故障修复状态，修复完成会自动恢复到运行中
 *
 * 运行状态下，只有所属势力玩家（且权限>=信标拥有者）能够打开界面
 * 其他状态下（启动中、已关闭、转让中），任何玩家都可打开界面
 *
 * 修复或关闭状态下可以选择转让势力，需要1分钟
 * 如果*原所属势力*玩家打开可以选择取消（其他玩家不能，避免多方争夺时过度混乱）
 * 转让会将拥有者设为点击按钮的玩家，且势力设为此玩家的主要势力
 * （界面第三个按钮可以取消转让）
 *
 * 暂定：修复状态下转让需要1分钟，平时转让需要30秒，
 * 首次启动15分钟（可取消），关闭10秒（可取消，完全关闭前其他玩家不能打开，防止手滑），
 * 之后再重启只花费1分钟（避免和平期间转让变得太过烦人）
 * 修复中持续30分钟（等于国战持续时间），修复完成后立即进入运行状态
 * 修复期间圈地不取消，但失去一切保护，如果转让成功圈地所属立刻变化（但仍无保护）
 *
 * 只有所属势力的玩家可以破坏（不论状态如何），如果没有所属势力则任何人都可破坏   - 已编写
 *
 * 如果原势力不存在了，或拥有者不在这个势力了，信标将自动失效并关闭
 *
 * 如果信标要声明的区域与其他领地重叠则不可启动
 */

/*
 * 升级需要继承AbstractClaimBeaconExtension类
 * 信标本体每数秒遍历下方方块，并在第一个非升级处停止
 *
 * 升级需要实现 apply(BeaconUpgradeLoader) 方法，
 * 并将自己的升级效果写入这个Loader对象
 * 信标在读取完所有升级后会缓存Loader取得的数据
 *
 * Loader包括增加/减少燃料消耗、增加圈地保护内容、增加回调函数之类的功能
 * 升级是由上至下应用的，因此后面的升级可以对前面的升级应用修改器，
 * 例如减少燃料消耗之类的
 *
 * 升级的拆除权限和信标一样，如果信标在运行时升级被拆，信标会故障并关机
 * （需要手动重启，但消耗是1分钟而不是15分钟）
 */
