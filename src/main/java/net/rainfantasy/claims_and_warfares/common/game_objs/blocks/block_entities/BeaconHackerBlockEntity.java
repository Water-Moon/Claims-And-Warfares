package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.BeaconLinkedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities.ClaimBeaconBlockEntity.BeaconHelper;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconHackerFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.BeaconHackerMenu;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockEntityRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BeaconHackerBlockEntity extends AbstractMachineBlockEntity {
	
	
	public static final int MAX_PROGRESS = CAWConstants.USE_TEST_TIMES ? 2 : 10;
	
	private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
			if (level == null || level.isClientSide) return;
			
			level.sendBlockUpdated(
			getBlockPos(), getBlockState(), getBlockState(), 3
			);
		}
	};
	private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
	protected final ContainerData data;
	
	public int tickCounter = 0;
	private BlockPos targetPos = new BlockPos(0, 0, 0);
	private boolean targetValid = false;
	private String targetOwner = "";
	private String targetFaction = "";
	private boolean enabled = false;
	private int progress = 0;
	private int progress2 = 0;
	private int maxProgress2 = 0;
	private int status = 0;
	private int remainingFuel = 0;
	private final Set<BlockPos> knownBeaconPos = new HashSet<>();
	
	public BeaconHackerBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityRegistry.BEACON_HACKER_BE.get(), pPos, pBlockState);
		
		this.data = new ContainerData() {
			@Override
			public int get(int i) {
				return 0;
			}
			
			@Override
			public void set(int i, int i1) {
			
			}
			
			@Override
			public int getCount() {
				return 0;
			}
		};
	}
	
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			return lazyItemHandler.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		lazyItemHandler = LazyOptional.of(() -> itemHandler);
	}
	
	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		lazyItemHandler.invalidate();
	}
	
	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("caw.gui.title.beacon_hacker");
	}
	
	@Override
	public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
		return new BeaconHackerMenu(i, inventory, this);
	}
	
	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (++this.tickCounter < 20) return;
		this.tickCounter = 0;
		this.rescanBeacons(level, pos, state);
		this.tryRefuel();
		this.tickProgress(level, pos, state);
		this.updateTargetInfo(level, pos, state);
		this.sendData();
	}
	
	private void tickProgress(Level level, BlockPos pos, BlockState state) {
		if (!this.enabled) return;
		BlockEntity target = level.getBlockEntity(this.targetPos);
		if (!(target instanceof ClaimBeaconBlockEntity beacon)) {
			this.enabled = false;
			return;
		}
		if (!BeaconHelper.isProperRunning(beacon.getStatus())) {
			this.enabled = false;
			return;
		}
		if (--this.remainingFuel <= 0) {
			this.enabled = false;
			return;
		}
		if (this.progress++ >= MAX_PROGRESS) {
			this.progress = 0;
			beacon.doHack(1);
		}
	}
	
	private void tryRefuel() {
		if (this.level == null || this.level.isClientSide) return;
		
		SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			inventory.setItem(i, this.itemHandler.getStackInSlot(i));
		}
		
		Optional<BeaconHackerFuelRecipe> recipe = this.level.getRecipeManager().getRecipeFor(BeaconHackerFuelRecipe.Type.INSTANCE, inventory, this.level);
		if (recipe.isEmpty()) return;
		int resultFuelAmount = this.remainingFuel + recipe.get().getFuelValue();
		if (resultFuelAmount > this.getMaxFuel()) return;
		
		this.itemHandler.extractItem(0, 1, false);
		this.remainingFuel = resultFuelAmount;
	}
	
	private void updateTargetInfo(Level level, BlockPos pos, BlockState state) {
		BlockEntity target = level.getBlockEntity(this.targetPos);
		if (!(target instanceof ClaimBeaconBlockEntity beacon)) {
			this.progress2 = 0;
			this.targetValid = false;
			this.targetOwner = "???";
			this.targetFaction = "???";
			return;
		}
		this.progress2 = beacon.getHackProgress();
		this.targetValid = true;
		this.maxProgress2 = beacon.getMaxHackProgress();
		this.targetOwner = beacon.getOwnerName();
		this.targetFaction = beacon.getOwningFactionName();
	}
	
	private void rescanBeacons(Level level, BlockPos pos, BlockState state) {
		Vector2i thisChunkPos = CoordUtil.blockToChunk(pos);
		this.knownBeaconPos.clear();
		CoordUtil.iterateCoords(thisChunkPos.sub(1, 1, new Vector2i()), thisChunkPos.add(1, 1, new Vector2i())).forEach(vec -> {
			ClaimDataManager.get().getClaimsAt(level, vec).forEach(data -> {
				data.getFeature(BeaconLinkedClaimFeature.class).ifPresent(feature -> {
					if (!(feature instanceof BeaconLinkedClaimFeature beaconLinkedClaimFeature)) {
						throw new IllegalArgumentException();
					}
					this.knownBeaconPos.add(beaconLinkedClaimFeature.getBeaconPos());
				});
			});
		});
	}
	
	@Override
	protected void saveAdditional(CompoundTag nbt) {
		nbt.putInt("tickCounter", this.tickCounter);
		nbt.putBoolean("enabled", this.enabled);
		nbt.putInt("progress", this.progress);
		nbt.putInt("progress2", this.progress2);
		nbt.putInt("maxProgress2", this.maxProgress2);
		nbt.putInt("status", this.status);
		nbt.putInt("fuel", this.remainingFuel);
		
		nbt.putIntArray("pos", new int[]{this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ()});
		nbt.putBoolean("tgtValid", this.targetValid);
		nbt.putString("tgtOwner", this.targetOwner);
		nbt.putString("tgtFaction", this.targetFaction);
		
		ListTag knownBeacons = new ListTag();
		for (BlockPos entry : this.knownBeaconPos) {
			knownBeacons.add(new IntArrayTag(new int[]{entry.getX(), entry.getY(), entry.getZ()}));
		}
		nbt.put("knownBeacons", knownBeacons);
		
		nbt.put("inventory", this.itemHandler.serializeNBT());
		
		super.saveAdditional(nbt);
	}
	
	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		
		this.tickCounter = nbt.getInt("tickCounter");
		this.enabled = nbt.getBoolean("enabled");
		this.progress = nbt.getInt("progress");
		this.progress2 = nbt.getInt("progress2");
		this.maxProgress2 = nbt.getInt("maxProgress2");
		this.status = nbt.getInt("status");
		this.remainingFuel = nbt.getInt("fuel");
		
		int[] pos = nbt.getIntArray("pos");
		this.targetPos = new BlockPos(pos[0], pos[1], pos[2]);
		this.targetValid = nbt.getBoolean("tgtValid");
		this.targetOwner = nbt.getString("tgtOwner");
		this.targetFaction = nbt.getString("tgtFaction");
		
		this.knownBeaconPos.clear();    //ARE YOU KIDDING ME???
		nbt.getList("knownBeacons", IntArrayTag.TAG_INT_ARRAY).forEach(tag -> {
			int[] vals = ((IntArrayTag) tag).getAsIntArray();
			this.knownBeaconPos.add(new BlockPos(vals[0], vals[1], vals[2]));
		});
		
		this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
	}
	
	@Override
	public AABB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
	
	//////// Getters ////////
	
	public Set<BlockPos> getKnownBeaconPos() {
		return knownBeaconPos;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public BlockPos getTargetPos() {
		return targetPos;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public int getProgress2() {
		return progress2;
	}
	
	public int getMaxProgress2() {
		return Math.max(1, maxProgress2);
	}
	
	public String getTargetFaction() {
		return targetFaction;
	}
	
	public String getTargetOwner() {
		return targetOwner;
	}
	
	public int getRemainingFuel() {
		return remainingFuel;
	}
	
	public int getMaxFuel() {
		return 120;
	}
	
	public boolean isTargetValid() {
		return targetValid;
	}
	
	//////// Setters ////////
	
	public static boolean validateAccess(ServerPlayer player, BeaconHackerBlockEntity block) {
		if (player.getEyePosition().distanceTo(block.getBlockPos().getCenter()) > 6) {
			player.sendSystemMessage(Component.translatable("caw.errors.generic.too_far"));
			return false;
		}
		return true;
	}
	
	public static void setTargetPos(ServerPlayer player, BlockPos target) {
		if (player == null) return;
		
		if (!(player.containerMenu instanceof BeaconHackerMenu menu)) return;
		BeaconHackerBlockEntity block = menu.block;
		
		if (!validateAccess(player, block)) return;
		
		if (!block.knownBeaconPos.contains(target)) {
			player.sendSystemMessage(Component.translatable("caw.errors.beacon_hacker.unknown_beacon"));
			return;
		}
		block.targetPos = target;
		block.sendData();
	}
	
	public static void turnOn(ServerPlayer player) {
		if (player == null) return;
		
		if (!(player.containerMenu instanceof BeaconHackerMenu menu)) return;
		BeaconHackerBlockEntity block = menu.block;
		
		if (!validateAccess(player, block)) return;
		
		block.enabled = true;
		block.sendData();
	}
	
	public static void turnOff(ServerPlayer player) {
		if (player == null) return;
		
		if (!(player.containerMenu instanceof BeaconHackerMenu menu)) return;
		BeaconHackerBlockEntity block = menu.block;
		
		if (!validateAccess(player, block)) return;
		
		block.enabled = false;
		block.sendData();
	}
}
