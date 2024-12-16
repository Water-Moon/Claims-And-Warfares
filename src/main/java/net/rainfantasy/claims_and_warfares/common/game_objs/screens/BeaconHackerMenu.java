package net.rainfantasy.claims_and_warfares.common.game_objs.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.BeaconHackerBlockEntity;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockRegistry;
import net.rainfantasy.claims_and_warfares.common.setups.registries.MenuRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BeaconHackerMenu extends AbstractMachineMenu<BeaconHackerBlockEntity> {
	
	public BeaconHackerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
		super(MenuRegistry.BEACON_HACKER_MENU.get(), containerId, inv, extraData);
	}
	
	public BeaconHackerMenu(int containerId, Inventory inv, BeaconHackerBlockEntity block) {
		super(MenuRegistry.BEACON_HACKER_MENU.get(), containerId, inv, block);
	}
	
	@Override
	protected BeaconHackerBlockEntity createOnClient(FriendlyByteBuf extraData) {
		ClientLevel l = Minecraft.getInstance().level;
		assert l != null;
		BlockEntity be = l.getBlockEntity(extraData.readBlockPos());
		if (be instanceof BeaconHackerBlockEntity b){
			b.load(Objects.requireNonNull(extraData.readNbt()));
			return b;
		}
		return null;
	}
	
	@Override
	protected void initContainer(BeaconHackerBlockEntity block) {
	
	}
	
	@Override
	protected void addSlots() {
		addVanillaInventory(inventory);
		
		this.block.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
			this.addSlot(new SlotItemHandler(iItemHandler, 0, 8, 117));
		});
	}
	
	@Override
	int getMachineSlotCount() {
		return 1;
	}
	
	@Override
	int getPlayerInventoryYPos() {
		return 147;
	}
	
	@Override
	int getHotbarYPos() {
		return 205;
	}
	
	@Override
	public boolean stillValid(@NotNull Player player) {
		return stillValid(ContainerLevelAccess.create(level, block.getBlockPos()), player, BlockRegistry.BEACON_HACKER.get());
	}
}
