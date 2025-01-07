package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BaseAdvancedBeaconUpgradeBlock;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BeaconUpgradeLoader;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_menus.BeaconDiplomaticUpgradeMenu;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.AbstractMachineBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockEntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.DiplomaticRelationshipData.*;

public class BeaconDiplomaticUpgradeBE extends AbstractMachineBlockEntity {
	
	int minInteractLevel = ALLY;
	int minPlaceBreakLevel = ALLY;
	
	protected final ContainerData data;
	
	public BeaconDiplomaticUpgradeBE(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityRegistry.BEACON_DIPLOMATIC_UPGRADE_BE.get(), pPos, pBlockState);
		
		// we do not use container data here because
		// we need to do faction verification similar to
		// what's done on the beacon block
		this.data = new ContainerData() {
			@Override
			public int get(int pIndex) {
				return 0;
			}
			
			@Override
			public void set(int pIndex, int pValue) {
			}
			
			@Override
			public int getCount() {
				return 0;
			}
		};
	}
	
	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
	}
	
	@Override
	public @NotNull Component getDisplayName() {
		return Component.literal("Diplomatic Upgrade");
	}
	
	@Override
	public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
		return new BeaconDiplomaticUpgradeMenu(pContainerId, pPlayerInventory, this);
	}
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt) {
		nbt.putInt("minInteractLevel", minInteractLevel);
		nbt.putInt("minPlaceBreakLevel", minPlaceBreakLevel);
		
		super.saveAdditional(nbt);
	}
	
	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		
		minInteractLevel = nbt.getInt("minInteractLevel");
		minPlaceBreakLevel = nbt.getInt("minPlaceBreakLevel");
	}
	
	public void setPermission(ServerPlayer user, int type, int level) {
		if (this.getLevel() == null) return;
		BaseAdvancedBeaconUpgradeBlock.findBeacon(this.getLevel(), this.getBlockPos()).ifPresent(beacon -> {
			if (!beacon.isOwnerAndFactionValid()) {
				user.sendSystemMessage(Component.translatable("caw.errors.beacon.not_set_properly"));
				return;
			}
			if (!FactionDataManager.get().isPermissionEqualOrHigher(beacon.getOwnerUUID(), user.getUUID(), beacon.getOwningFactionUUID())) {
				user.sendSystemMessage(Component.translatable("caw.errors.beacon.lower_permission"));
				return;
			}
			if (level != ALLY && level != ENEMY && level != NEUTRAL && level != OWNER) {
				return;
			}
			if (type == FactionOwnedClaimFeature.INTERACT) minInteractLevel = level;
			else if (type == FactionOwnedClaimFeature.BREAK_PLACE) minPlaceBreakLevel = level;
			this.refresh();
		});
	}
	
	public int getInteractPermission() {
		return minInteractLevel;
	}
	
	public int getPlaceBreakPermission() {
		return minPlaceBreakLevel;
	}
	
	public void apply(BeaconUpgradeLoader loader) {
		loader.setBreakPlaceDiplomaticLevel(minPlaceBreakLevel);
		loader.setInteractDiplomaticLevel(minInteractLevel);
		loader.increaseFuelCost(minPlaceBreakLevel != ALLY ? 1 : 0);
		loader.increaseFuelCost(minInteractLevel != ALLY ? 1 : 0);
	}
	
	
}
