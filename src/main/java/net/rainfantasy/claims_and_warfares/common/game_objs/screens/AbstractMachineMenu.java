package net.rainfantasy.claims_and_warfares.common.game_objs.screens;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMachineMenu<T extends BlockEntity> extends AbstractContainerMenu {
	
	public Level level;
	public T block;
	public Player player;
	public Inventory inventory;
	
	protected AbstractMachineMenu(@Nullable MenuType<?> menu, int containerId, Inventory inv, FriendlyByteBuf extraData) {
		super(menu, containerId);
		init(inv, createOnClient(extraData));
	}
	
	protected AbstractMachineMenu(@Nullable MenuType<?> menu, int containerId, Inventory inv, T block) {
		super(menu, containerId);
		init(inv, block);
	}
	
	protected void init(Inventory inventory, T block) {
		this.player = inventory.player;
		this.level = player.level();
		this.inventory = inventory;
		this.block = block;
		this.initContainer(block);
		this.addSlots();
		this.broadcastChanges();
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract T createOnClient(FriendlyByteBuf extraData);
	
	protected abstract void initContainer(T block);
	
	protected abstract void addSlots();
	
	public int getMachineSlotCount() {
		return 0;
	}
	
	public int getVanillaSlotCount() {
		return 36;
	}
	
	public int getPlayerInventoryXPos() {
		return 8;
	}
	
	public int getPlayerInventoryYPos() {
		return 84;
	}
	
	public int getHotbarYPos() {
		return 142;
	}
	
	public void addVanillaInventory(Inventory playerInventory) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, this.getPlayerInventoryXPos() + j * 18, this.getPlayerInventoryYPos() + i * 18));
			}
		}
		
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(playerInventory, i, this.getPlayerInventoryXPos() + i * 18, this.getHotbarYPos()));
		}
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int clickedSlot) {
		Slot fromSlot = this.slots.get(clickedSlot);
		if (!fromSlot.hasItem()) return ItemStack.EMPTY;
		
		ItemStack sourceStack = fromSlot.getItem();
		ItemStack copy = sourceStack.copy();
		
		if (clickedSlot >= this.getVanillaSlotCount()) {
			//moving from machine to player inventory
			if (clickedSlot >= this.getVanillaSlotCount() + this.getMachineSlotCount()) {
				CAWConstants.LOGGER.error("Invalid slot index: {}", clickedSlot);
				return ItemStack.EMPTY;
			}
			if (!moveItemStackTo(sourceStack, 0, this.getVanillaSlotCount(), false)) {
				return ItemStack.EMPTY;
			}
		} else {
			//moving from player inventory to machine
			if (!moveItemStackTo(sourceStack, this.getVanillaSlotCount(), this.getVanillaSlotCount() + this.getMachineSlotCount(), false)) {
				return ItemStack.EMPTY;
			}
		}
		
		if (sourceStack.getCount() == 0) {
			fromSlot.set(ItemStack.EMPTY);
		} else {
			fromSlot.setChanged();
		}
		fromSlot.onTake(player, sourceStack);
		return copy;
	}
}
