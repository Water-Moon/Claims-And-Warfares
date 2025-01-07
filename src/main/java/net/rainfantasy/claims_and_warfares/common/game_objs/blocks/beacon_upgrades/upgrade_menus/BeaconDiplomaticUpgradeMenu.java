package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_menus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.upgrade_block_entities.BeaconDiplomaticUpgradeBE;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.AbstractMachineMenu;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import net.rainfantasy.claims_and_warfares.common.setups.registries.MenuRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BeaconDiplomaticUpgradeMenu extends AbstractMachineMenu<BeaconDiplomaticUpgradeBE> {
	
	public BeaconDiplomaticUpgradeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		super(MenuRegistry.BEACON_DIPLOMATIC_UPGRADE_MENU.get(), containerId, inv, extraData);
	}
	
	public BeaconDiplomaticUpgradeMenu(int containerId, Inventory inv, BeaconDiplomaticUpgradeBE block) {
		super(MenuRegistry.BEACON_DIPLOMATIC_UPGRADE_MENU.get(), containerId, inv, block);
	}
	
	@Override
	protected BeaconDiplomaticUpgradeBE createOnClient(FriendlyByteBuf extraData) {
		ClientLevel l = Minecraft.getInstance().level;
		assert l != null;
		BlockEntity be = l.getBlockEntity(extraData.readBlockPos());
		if (be instanceof BeaconDiplomaticUpgradeBE b) {
			b.load(Objects.requireNonNull(extraData.readNbt()));
			return b;
		}
		return null;
	}
	
	@Override
	protected void initContainer(BeaconDiplomaticUpgradeBE block) {
	}
	
	@Override
	protected void addSlots() {
	}
	
	@Override
	public int getVanillaSlotCount() {
		return 0;
	}
	
	@Override
	public void addVanillaInventory(Inventory playerInventory) {
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int clickedSlot) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, block.getBlockPos()), player, BlockRegistry.BEACON_UPGRADE_DIPLOMATIC.get());
	}
}
