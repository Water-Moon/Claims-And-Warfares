package net.rainfantasy.claims_and_warfares.common.game_objs.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import net.rainfantasy.claims_and_warfares.common.setups.registries.MenuRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClaimBeaconMenu extends AbstractMachineMenu<ClaimBeaconBlockEntity> {
	
	
	public ClaimBeaconMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		super(MenuRegistry.CLAIM_BEACON_MENU.get(), containerId, inv, extraData);
	}
	
	public ClaimBeaconMenu(int containerId, Inventory inv, ClaimBeaconBlockEntity block) {
		super(MenuRegistry.CLAIM_BEACON_MENU.get(), containerId, inv, block);
	}
	
	@Override
	protected ClaimBeaconBlockEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel l = Minecraft.getInstance().level;
		assert l != null;
		BlockEntity be = l.getBlockEntity(extraData.readBlockPos());
		if (be instanceof ClaimBeaconBlockEntity b) {
			b.load(Objects.requireNonNull(extraData.readNbt()));
			return b;
		}
		return null;
	}
	
	@Override
	protected void initContainer(ClaimBeaconBlockEntity block) {
	
	}
	
	@Override
	protected void addSlots() {
		addVanillaInventory(inventory);
		
		this.block.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
			this.addSlot(new SlotItemHandler(iItemHandler, 0, 134, 106));
		});
		
	}
	
	@Override
	public int getMachineSlotCount() {
		return 1;
	}
	
	@Override
	public int getPlayerInventoryYPos() {
		return 147;
	}
	
	@Override
	public int getHotbarYPos() {
		return 205;
	}
	
	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, block.getBlockPos()), player, BlockRegistry.CLAIM_BEACON.get());
	}
	
	public String getOwnerName() {
		return block.getOwnerName();
	}
	
	public String getFactionName() {
		return block.getOwningFactionName();
	}
	
	public int getClaimSize() {
		return block.getClaimSize();
	}
}
