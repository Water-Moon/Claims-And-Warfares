package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.AbstractClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.BeaconLinkedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.FactionOwnedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionData;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.misc.OfflinePlayerDatabase;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.AbstractBeaconUpgradeBlock;
import net.rainfantasy.claims_and_warfares.common.game_objs.blocks.beacon_upgrades.BeaconUpgradeLoader;
import net.rainfantasy.claims_and_warfares.common.game_objs.recipes.BeaconFuelRecipe;
import net.rainfantasy.claims_and_warfares.common.game_objs.screens.ClaimBeaconMenu;
import net.rainfantasy.claims_and_warfares.common.setups.data_types.SerializableDateTime;
import net.rainfantasy.claims_and_warfares.common.setups.registries.BlockEntityRegistry;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClaimBeaconBlockEntity extends AbstractMachineBlockEntity {
	
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
	private UUID linkedClaimUUID = CAWConstants.NIL_UUID;
	private UUID linkedClaimFeatureUUID = CAWConstants.NIL_UUID;
	private UUID owningFactionUUID = CAWConstants.NIL_UUID;
	private UUID newOwningFactionUUID = CAWConstants.NIL_UUID;
	private UUID newOwnerUUID = CAWConstants.NIL_UUID;
	private String owningFactionName = "";
	private UUID ownerUUID = CAWConstants.NIL_UUID;
	private String ownerName = "";
	private int upgradeAmount = 0;
	private int remainingFuel = 0;
	private int claimSize = 1;
	private int tickCounter = 0;
	private int color = 16777215;
	private int timer = 0;
	private int timer2 = 0;
	private int status = 0;
	private int errorCode = 0;
	private int hackProgress = 0;
	private int hackDecreaseTimer = 0;
	private boolean fullyInitialized = false;
	private SerializableDateTime eventTime = new SerializableDateTime();
	
	private BeaconUpgradeLoader upgradeData = new BeaconUpgradeLoader();
	
	public static final int ERROR_NO_FUEL = 1;
	public static final int ERROR_NO_OWNER = 2;
	public static final int ERROR_CLAIM_CONFLICT = 3;
	public static final int ERROR_OBSTRUCTED = 4;
	public static final int ERROR_UPGRADE_CHANGED = 5;
	
	public static final int STATUS_OFF = 0;
	public static final int STATUS_STARTING = 1;
	public static final int STATUS_STOPPING = 2;
	public static final int STATUS_RUNNING = 11;
	public static final int STATUS_ERRORED_OFF = 20;
	public static final int STATUS_ERRORED_STOPPING = 22;
	public static final int STATUS_UNSTABLE_REPAIRING = 30;
	public static final int STATUS_UNSTABLE_RUNNING = 31;
	public static final int STATUS_UNSTABLE_STOPPING = 32;
	public static final int STATUS_UNSTABLE_CHANGING_FACTION = 33;
	public static final int STATUS_CHANGING_FACTION = 91;
	
	public ClaimBeaconBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityRegistry.CLAIM_BEACON_BE.get(), pos, state);
		
		this.data = new ClaimBeaconContainerData();
	}
	
	public long getUnstableTime() {
		if (this.status != STATUS_UNSTABLE_RUNNING) {
			return 0;
		}
		return Math.max(0, eventTime.getDiffMillis(SerializableDateTime.now()) / 1000);
	}
	
	@SuppressWarnings({"DuplicateBranchesInSwitch", "LoggingSimilarMessage"})
	public int getTransientTime(int status) {
		if(CAWConstants.USE_TEST_TIMES) {
			switch (status) {
				case STATUS_OFF, STATUS_ERRORED_OFF -> {
					return -1;
				}
				case STATUS_STARTING -> {
					return this.fullyInitialized ? 2 : 5;
				}
				case STATUS_STOPPING, STATUS_ERRORED_STOPPING, STATUS_UNSTABLE_STOPPING -> {
					return 2;
				}
				case STATUS_UNSTABLE_REPAIRING -> {
					return 30;
				}
				case STATUS_UNSTABLE_RUNNING -> {
					CAWConstants.LOGGER.error("Time remaining for unstable running phase should be obtained by calling getUnstableTime()!");
					CAWConstants.LOGGER.error("Stacktrace: ", new IllegalArgumentException());
					return -1;
				}
				case STATUS_UNSTABLE_CHANGING_FACTION -> {
					return 5;
				}
				case STATUS_CHANGING_FACTION -> {
					return 2;
				}
				default -> {
					CAWConstants.LOGGER.error("Unknown claim beacon status {}", status);
					CAWConstants.LOGGER.error("Stacktrace: ", new IllegalArgumentException());
					return -1;
				}
			}
		}else{
			switch (status) {
				case STATUS_OFF, STATUS_ERRORED_OFF -> {
					return -1;
				}
				case STATUS_STARTING -> {
					return this.fullyInitialized ? 60 : 15*60;
				}
				case STATUS_STOPPING, STATUS_ERRORED_STOPPING, STATUS_UNSTABLE_STOPPING -> {
					return 10;
				}
				case STATUS_UNSTABLE_REPAIRING -> {
					return 30*60;
				}
				case STATUS_UNSTABLE_RUNNING -> {
					CAWConstants.LOGGER.error("Time remaining for unstable running phase should be obtained by calling getUnstableTime()!");
					CAWConstants.LOGGER.error("Stacktrace: ", new IllegalArgumentException());
					return -1;
				}
				case STATUS_UNSTABLE_CHANGING_FACTION -> {
					return 60;
				}
				case STATUS_CHANGING_FACTION -> {
					return 30;
				}
				default -> {
					CAWConstants.LOGGER.error("Unknown claim beacon status {}", status);
					CAWConstants.LOGGER.error("Stacktrace: ", new IllegalArgumentException());
					return -1;
				}
			}
			
		}
	}
	
	public int getHackDecreaseTime() {
		return 5;
	}
	
	public int getHackDecreaseTimeAfterAttack() {
		return 30;
	}
	
	public int getMaxHackProgress() {
		return 100;
	}
	
	private SerializableDateTime getDeactivateTime() {
		if(CAWConstants.USE_TEST_TIMES){
			return new SerializableDateTime().plusSeconds(10);
		}else {
			return new SerializableDateTime().toNextSpecificHourWithAtLeastIntervalHourOrElseNextDay(20, 8);
		}
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
		return Component.translatable("caw.gui.title.claim_beacon");
	}
	
	@Override
	public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
		return new ClaimBeaconMenu(i, inventory, this);
	}
	
	//////// Tick Logic ////////
	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		this.tickCounter++;
		if (this.tickCounter < 20) return;
		this.tickCounter = 0;
		this.tickState(level, pos, state);
		this.sendData();
	}
	
	@SuppressWarnings("DuplicateBranchesInSwitch")
	public void tickState(Level level, BlockPos pos, BlockState state) {
		this.refreshName();
		this.tickHackProgress();
		this.tryRefuel();
		this.checkUpgrade(level, pos, state);
		this.applyUpgrade(level, pos, state);
		if (BeaconHelper.isUnstableState(this.status)) {
			this.checkSkyView(level, pos, state, true);
		}
		switch (this.status) {
			case STATUS_OFF:
				break;
			case STATUS_STARTING:
				if (!validateNoConflict(level, pos, state)) {
					break;
				}
				if (--timer <= 0) {
					handleStartingComplete(level, pos, state);
				}
				break;
			case STATUS_STOPPING:
				if (--timer <= 0) {
					handleStoppingComplete(level, pos, state);
				}
				break;
			case STATUS_RUNNING:
				if (!this.validateOwnerFaction(level, pos, state)) {
					break;
				}
				if (!this.checkSkyView(level, pos, state, false)) {
					break;
				}
				tickRunning(level, pos, state);
				break;
			case STATUS_ERRORED_OFF:
				break;
			case STATUS_ERRORED_STOPPING:
				if (--timer <= 0) {
					handleErroredStoppingComplete(level, pos, state);
				}
				break;
			case STATUS_UNSTABLE_REPAIRING:
				if (--timer <= 0) {
					handleRepairComplete(level, pos, state);
				}
				break;
			case STATUS_UNSTABLE_RUNNING:
				tickUnstableRunning(level, pos, state);
				break;
			case STATUS_UNSTABLE_STOPPING:
				if (--timer <= 0) {
					handleUnstableStopped(level, pos, state);
				}
				break;
			case STATUS_UNSTABLE_CHANGING_FACTION:
				if (--timer <= 0) {
					handleRepairComplete(level, pos, state);
					break;
				}
				if (--timer2 <= 0) {
					handleUnstableFactionChange(level, pos, state);
				}
				break;
			case STATUS_CHANGING_FACTION:
				if (--timer <= 0) {
					handleFactionChange(level, pos, state);
				}
			default:
				break;
		}
	}
	
	private boolean validateNoConflict(Level level, BlockPos pos, BlockState state) {
		Vector2i chunkCoord = CoordUtil.blockToChunk(pos);
		int actualSize = this.claimSize - 1;
		AtomicBoolean result = new AtomicBoolean(true);
		CoordUtil.iterateCoords(chunkCoord.sub(actualSize, actualSize, new Vector2i()), chunkCoord.add(actualSize, actualSize, new Vector2i()))
		.forEach(chunk -> {
			if (ClaimDataManager.get().getClaimsAt(level, chunk).stream().anyMatch(claim -> {
				if(claim.hasFeature(BeaconLinkedClaimFeature.class) && (!claim.getUUID().equals(this.linkedClaimUUID))){
					return true;
				}
				if(claim.getAllFeatures().stream().anyMatch(AbstractClaimFeature::conflictWithFactionClaims)) {
					return true;
				}
				return false;
			})) {
				this.status = STATUS_ERRORED_STOPPING;
				this.errorCode = ERROR_CLAIM_CONFLICT;
				this.timer = getTransientTime(STATUS_ERRORED_STOPPING);
				result.set(false);
			}
		});
		return result.get();
	}
	
	private void handleStartingComplete(Level level, BlockPos pos, BlockState state) {
		this.timer = 0;
		this.createLinkedClaim(level, pos, state);
		this.setLinkedClaimProtection(level, pos, state, true);
		this.status = STATUS_RUNNING;
		this.fullyInitialized = true;
	}
	
	private void handleStoppingComplete(Level level, BlockPos pos, BlockState state) {
		this.timer = 0;
		this.removeLinkedClaim(level, pos, state);
		this.status = STATUS_OFF;
	}
	
	private void tickRunning(Level level, BlockPos pos, BlockState state) {
		if (!this.consumeFuel()) {
			this.status = STATUS_ERRORED_STOPPING;
			this.errorCode = ERROR_NO_FUEL;
			this.timer = getTransientTime(STATUS_ERRORED_STOPPING);
		}
		this.validateLinkedClaim(level, pos, state);
	}
	
	private void handleErroredStoppingComplete(Level level, BlockPos pos, BlockState state) {
		this.timer = 0;
		this.removeLinkedClaim(level, pos, state);
		this.status = STATUS_ERRORED_OFF;
	}
	
	private void handleRepairComplete(Level level, BlockPos pos, BlockState state) {
		this.timer = 0;
		this.timer2 = 0;
		this.status = STATUS_RUNNING;
		this.newOwningFactionUUID = CAWConstants.NIL_UUID;
		this.newOwnerUUID = CAWConstants.NIL_UUID;
		this.setLinkedClaimProtection(level, pos, state, true);
	}
	
	private void tickUnstableRunning(Level level, BlockPos pos, BlockState state) {
		this.consumeFuel();
		if (this.getUnstableTime() == 0) {
			this.handleUnstableStopping(level, pos, state);
		}
		this.validateLinkedClaim(level, pos, state);
		
		//honestly I really hope no one tries to game the system, but just in case...
		if ((!this.isOwnerValid()) && this.isFactionValid()) {
			//faction still exist but owner may have quit the faction
			//likely to game the system, so we just assign a new owner from the faction
			this.newOwnerUUID = FactionDataManager.get().getFaction(this.owningFactionUUID).orElseThrow().getOwnerUUID();
			this.refreshName();
		} else if (!this.isFactionValid()) {
			//faction disbanded, we just remove the claim
			
			//the beacon should blow up to prevent abuse (in the form of
			//disbanding and reforming faction to avoid the fight)
			this.status = STATUS_ERRORED_STOPPING;
			this.errorCode = ERROR_NO_OWNER;
			this.timer = getTransientTime(STATUS_ERRORED_STOPPING);
			level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 16, true, ExplosionInteraction.BLOCK);
		}
	}
	
	private void handleUnstableStopping(Level level, BlockPos pos, BlockState state) {
		this.timer = getTransientTime(STATUS_UNSTABLE_STOPPING);
		this.status = STATUS_UNSTABLE_STOPPING;
	}
	
	private void handleUnstableStopped(Level level, BlockPos pos, BlockState state) {
		this.timer = getTransientTime(STATUS_UNSTABLE_REPAIRING);
		this.setLinkedClaimProtection(level, pos, state, false);
		this.status = STATUS_UNSTABLE_REPAIRING;
	}
	
	private void handleUnstableFactionChange(Level level, BlockPos pos, BlockState state) {
		this.timer2 = 0;
		this.status = STATUS_UNSTABLE_REPAIRING;
		
		this.removeLinkedClaim(level, pos, state);
		this.ownerUUID = this.newOwnerUUID;
		this.owningFactionUUID = this.newOwningFactionUUID;
		this.newOwningFactionUUID = CAWConstants.NIL_UUID;
		this.newOwnerUUID = CAWConstants.NIL_UUID;
		this.createLinkedClaim(level, pos, state);
		this.setLinkedClaimProtection(level, pos, state, false);
		this.refreshName();
	}
	
	private void handleFactionChange(Level level, BlockPos pos, BlockState state) {
		this.timer = 0;
		this.status = STATUS_OFF;
		
		this.ownerUUID = this.newOwnerUUID;
		this.owningFactionUUID = this.newOwningFactionUUID;
		this.newOwningFactionUUID = CAWConstants.NIL_UUID;
		this.newOwnerUUID = CAWConstants.NIL_UUID;
		this.refreshName();
	}
	
	public boolean consumeFuel() {
		this.remainingFuel -= this.getFuelConsumption();
		if (this.remainingFuel <= 0) {
			this.remainingFuel = 0;
			return false;
		}
		return true;
		//return true;
	}
	
	public int getFuelConsumption() {
		return 1 + this.upgradeData.getIncreaseFuelCost();
	}
	
	private void tryRefuel() {
		if (this.level == null || this.level.isClientSide) return;
		
		SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			inventory.setItem(i, this.itemHandler.getStackInSlot(i));
		}
		
		Optional<BeaconFuelRecipe> recipe = this.level.getRecipeManager().getRecipeFor(BeaconFuelRecipe.Type.INSTANCE, inventory, this.level);
		if (recipe.isEmpty()) return;
		int resultFuelAmount = this.remainingFuel + recipe.get().getFuelValue();
		if (resultFuelAmount > this.getMaxFuel()) return;
		
		this.itemHandler.extractItem(0, 1, false);
		this.remainingFuel = resultFuelAmount;
	}
	
	public void tickHackProgress() {
		if (this.hackProgress <= 0) return;
		if (!BeaconHelper.isProperRunning(this.status)) return;
		
		if (this.hackProgress >= getMaxHackProgress()) {
			this.hackProgress = 0;
			this.hackDecreaseTimer = 0;
			this.status = STATUS_UNSTABLE_RUNNING;
			this.eventTime = getDeactivateTime();
			return;
		}
		--this.hackDecreaseTimer;
		if (this.hackDecreaseTimer == 0) {
			if (--this.hackProgress > 0) {
				this.hackDecreaseTimer = getHackDecreaseTime();
			}
		}
	}
	
	public boolean checkSkyView(Level level, BlockPos pos, BlockState state, boolean forceRemove) {
		Vector2i start = new Vector2i(pos.getX(), pos.getZ()).sub(1, 1);
		Vector2i end = new Vector2i(pos.getX(), pos.getZ()).add(1, 1);
		AtomicBoolean flag = new AtomicBoolean(true);
		CoordUtil.iterateCoords(start, end).forEach(coord -> {
			int yPos = CoordUtil.getTopBlockHeight(level, new BlockPos(coord.x(), 0, coord.y()), false);
			if (yPos <= pos.getY()) return;
			if (forceRemove) {
				level.destroyBlock(new BlockPos(coord.x(), yPos, coord.y()), false);
				level.explode(null, coord.x(), yPos, coord.y(), 2, true, ExplosionInteraction.BLOCK);
			} else {
				flag.set(false);
				this.status = STATUS_ERRORED_STOPPING;
				this.timer = getTransientTime(STATUS_ERRORED_STOPPING);
				this.errorCode = ERROR_OBSTRUCTED;
			}
		});
		return flag.get();
	}
	
	public void doHack(int amount) {
		this.hackProgress += amount;
		this.hackDecreaseTimer = getHackDecreaseTimeAfterAttack();
	}
	
	private void checkUpgrade(Level level, BlockPos pos, BlockState state) {
		ArrayList<BlockPos> knownUpgrade = new ArrayList<>();
		pos = pos.below();
		while (level.getBlockState(pos).getBlock() instanceof AbstractBeaconUpgradeBlock) {
			knownUpgrade.add(pos);
			pos = pos.below();
		}
		
		BeaconUpgradeLoader newUpgradeData = new BeaconUpgradeLoader();
		knownUpgrade.forEach(upgradePos -> {
			if (level.getBlockState(upgradePos).getBlock() instanceof AbstractBeaconUpgradeBlock upgradeBlock) {
				newUpgradeData.doApply(upgradeBlock);
			}
		});
		
		if (!this.upgradeData.hasSameUpgrade(newUpgradeData)) {
			//upgrade changed
			
			if (BeaconHelper.isUnstableState(this.status)) {
				//do not allow adding upgrade during unstable state
				for (int i = this.upgradeData.getTotalUpgradeCount(); i < knownUpgrade.size(); i++) {
					level.destroyBlock(knownUpgrade.get(i), false);
					level.explode(null, knownUpgrade.get(i).getX(), knownUpgrade.get(i).getY(), knownUpgrade.get(i).getZ(), 2, true, ExplosionInteraction.BLOCK);
				}
			} else if (BeaconHelper.isProperRunning(this.status)) {
				this.status = STATUS_ERRORED_STOPPING;
				this.errorCode = ERROR_UPGRADE_CHANGED;
			} else {
				this.upgradeData = newUpgradeData;
			}
		}
	}
	
	private void applyUpgrade(Level level, BlockPos pos, BlockState state) {
		this.claimSize = 1 + this.upgradeData.getSizeIncrease();
		this.upgradeAmount = this.upgradeData.getTotalUpgradeCount();
		if (this.shouldMaintainClaim()) {
			ClaimDataManager.get().getClaim(this.linkedClaimUUID)
			.flatMap(claim -> claim.getFeatureCheckType(this.getLinkedClaimFeatureUUID(), BeaconLinkedClaimFeature.class))
			.ifPresent(feature -> {
				((BeaconLinkedClaimFeature) feature).setProtectExplosions(this.upgradeData.isExplosionProtection());
				((BeaconLinkedClaimFeature) feature).setProtectMobGriefing(this.upgradeData.isMobGriefProtection());
			});
		}
	}
	
	
	//////// Validation ////////
	
	private boolean hasLinkedClaim() {
		return (this.linkedClaimUUID != CAWConstants.NIL_UUID && this.linkedClaimFeatureUUID != CAWConstants.NIL_UUID);
	}
	
	@CheckReturnValue
	private boolean validateOwnerFaction(Level level, BlockPos pos, BlockState state) {
		if (!(this.isOwnerValid() && this.isFactionValid())) {
			onValidateFail(level, pos, state);
			return false;
		}
		AtomicBoolean flag = new AtomicBoolean(true);
		FactionDataManager.get().getFaction(this.owningFactionUUID).ifPresentOrElse(factionData -> {
			if (!FactionDataManager.get().isPlayerInFaction(this.ownerUUID, this.owningFactionUUID)) {
				onValidateFail(level, pos, state);
				flag.set(false);
			}
		}, () -> {
			onValidateFail(level, pos, state);
			flag.set(false);
		});
		return flag.get();
	}
	
	private void onValidateFail(Level level, BlockPos pos, BlockState state) {
		if (this.status == STATUS_RUNNING) {
			this.status = STATUS_ERRORED_STOPPING;
			this.errorCode = ERROR_NO_OWNER;
			this.timer = getTransientTime(STATUS_ERRORED_STOPPING);
			this.removeLinkedClaim(level, pos, state);
			this.owningFactionUUID = CAWConstants.NIL_UUID;
		}
	}
	
	private void setLinkedClaimProtection(Level level, BlockPos pos, BlockState state, boolean doProtect) {
		ClaimDataManager.get().getClaim(this.linkedClaimUUID)
		.flatMap(claim -> claim.getFeatureCheckType(this.linkedClaimFeatureUUID, BeaconLinkedClaimFeature.class))
		.ifPresent(feature -> {
			if (feature instanceof BeaconLinkedClaimFeature beaconFeature) {
				beaconFeature.setEnabled(doProtect);
			}
		});
	}
	
	private void validateLinkedClaim(Level level, BlockPos pos, BlockState state) {
		ClaimDataManager.get().getClaim(this.linkedClaimUUID).ifPresentOrElse(claim -> {
			if (claim.getFeatures(BeaconLinkedClaimFeature.class).stream().noneMatch(feature -> feature.getUUID().equals(this.linkedClaimFeatureUUID))) {
				CAWConstants.LOGGER.error("Linked claim feature {} of claim {} for beacon {} disappeared!", this.linkedClaimFeatureUUID, this.linkedClaimUUID, this.getBlockPos());
				BeaconLinkedClaimFeature feature = new BeaconLinkedClaimFeature(pos, claim.getUUID(), this.claimSize);
				this.linkedClaimFeatureUUID = feature.getUUID();
				claim.addClaimFeature(feature);
			}
		}, () -> {
			if (FactionDataManager.get().getFaction(this.owningFactionUUID).isPresent()) {
				//warning only if faction present because otherwise the claim might have been removed
				CAWConstants.LOGGER.error("Linked claim {} of beacon {} disappeared!", this.linkedClaimUUID, this.getBlockPos());
			}
			createLinkedClaim(level, pos, state);
		});
	}
	
	private void createLinkedClaim(Level level, BlockPos pos, BlockState state) {
		ClaimData newClaim = new ClaimData(level);
		this.linkedClaimUUID = newClaim.getUUID();
		
		BeaconLinkedClaimFeature feature = new BeaconLinkedClaimFeature(pos, newClaim.getUUID(), this.claimSize);
		this.linkedClaimFeatureUUID = feature.getUUID();
		newClaim.addClaimFeature(feature);
		newClaim.addClaimFeature(new FactionOwnedClaimFeature(this.owningFactionUUID));
		
		Vector2i chunkPos = CoordUtil.blockToChunk(pos);
		int claimSizeActual = this.claimSize - 1;
		CoordUtil.iterateCoords(chunkPos.sub(claimSizeActual, claimSizeActual, new Vector2i()), chunkPos.add(claimSizeActual, claimSizeActual, new Vector2i()))
		.forEach(newClaim::claimChunk);
		
		ClaimDataManager.get().addClaim(newClaim);
		FactionClaimDataManager.get().addClaimTo(this.owningFactionUUID, newClaim.getUUID());
	}
	
	private void removeLinkedClaim(Level level, BlockPos pos, BlockState state) {
		Optional<ClaimData> data = ClaimDataManager.get().getClaim(this.linkedClaimUUID);
		if (data.isEmpty()) {
			return;
		}
		FactionClaimDataManager.get().removeClaimOf(this.owningFactionUUID, this.linkedClaimUUID);
		ClaimDataManager.get().removeClaim(this.linkedClaimUUID);
		this.linkedClaimUUID = CAWConstants.NIL_UUID;
		this.linkedClaimFeatureUUID = CAWConstants.NIL_UUID;
	}
	
	public void refreshName() {
		if (isFactionValid()) {
			FactionDataManager.get().getFaction(this.owningFactionUUID).ifPresentOrElse(data -> {
				this.owningFactionName = data.getFactionName();
				this.color = data.getFactionColor();
			}, () -> {
				throw new IllegalStateException("Faction valid but name not found!");
			});
		} else {
			this.owningFactionName = "";
		}
		if (isOwnerValid()) {
			OfflinePlayerDatabase.get().getName(this.ownerUUID).ifPresentOrElse(
			name -> this.ownerName = name,
			() -> this.ownerName = "???");
		}
	}
	
	//true if the owning faction do exist
	public boolean isFactionValid() {
		return FactionDataManager.get().getFaction(this.owningFactionUUID).isPresent();
	}
	
	public boolean isFactionValidClientSide() {
		return this.owningFactionUUID != CAWConstants.NIL_UUID;
	}
	
	//true if the owner exist, and either faction don't exist, or the owner is in the faction
	public boolean isOwnerValid() {
		if (this.ownerUUID.equals(CAWConstants.NIL_UUID)) return false;
		if (!this.owningFactionUUID.equals(CAWConstants.NIL_UUID)) {
			if (FactionDataManager.get().getFaction(this.owningFactionUUID).isEmpty()) return false;
			if (!FactionDataManager.get().isPlayerInFaction(this.ownerUUID, this.owningFactionUUID)) return false;
		} else {
			return true;
		}
		return true;
	}
	
	public boolean isOwnerAndFactionValid() {
		return this.isOwnerValid() && this.isFactionValid();
	}
	
	public void tryBindFaction(ServerPlayer player) {
		Optional<FactionData> primaryFaction = FactionDataManager.get().getPrimaryFaction(this.ownerUUID);
		primaryFaction.ifPresentOrElse(data -> {
			this.owningFactionUUID = data.getFactionUUID();
			this.owningFactionName = data.getFactionName();
			player.sendSystemMessage(Component.translatable("caw.message.beacon.faction_assigned", this.owningFactionName));
		}, () -> {
			this.owningFactionName = "";
			this.owningFactionUUID = CAWConstants.NIL_UUID;
		});
		this.setChanged();
	}
	
	public void tryBindPlayer(ServerPlayer player) {
		this.ownerUUID = player.getUUID();
		this.ownerName = player.getScoreboardName();
		player.sendSystemMessage(Component.translatable("caw.message.beacon.owner_assigned"));
	}
	
	
	public boolean shouldMaintainClaim() {
		return this.status == STATUS_RUNNING ||
		       this.status == STATUS_STOPPING ||
		       this.status == STATUS_UNSTABLE_RUNNING ||
		       this.status == STATUS_UNSTABLE_CHANGING_FACTION ||
		       this.status == STATUS_UNSTABLE_REPAIRING ||
		       this.status == STATUS_UNSTABLE_STOPPING;
	}
	
	//////// Load/Save ////////
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt) {
		nbt.putString("claimUUID", this.linkedClaimUUID.toString());
		nbt.putString("claimFeatureUUID", this.linkedClaimFeatureUUID.toString());
		nbt.putString("factionUUID", this.owningFactionUUID.toString());
		nbt.putString("newOwningFactionUUID", this.newOwningFactionUUID.toString());
		nbt.putString("newOwnerUUID", this.newOwnerUUID.toString());
		nbt.putString("factionName", this.owningFactionName);
		nbt.putString("ownerUUID", this.ownerUUID.toString());
		nbt.putString("ownerName", this.ownerName);
		nbt.putInt("upgradeAmount", this.upgradeAmount);
		nbt.putInt("color", this.color);
		nbt.putInt("fuel", this.remainingFuel);
		nbt.putInt("claimSize", this.claimSize);
		nbt.putInt("tickCounter", this.tickCounter);
		nbt.putInt("timer", this.timer);
		nbt.putInt("timer2", this.timer2);
		nbt.putInt("status", this.status);
		nbt.putInt("errorCode", this.errorCode);
		nbt.putInt("hackProgress", this.hackProgress);
		nbt.putInt("hackDecreaseTimer", this.hackDecreaseTimer);
		nbt.putBoolean("fullyInitialized", this.fullyInitialized);
		nbt.put("eventTime", eventTime.writeToNBT(new CompoundTag()));
		
		nbt.put("upgradeData", this.upgradeData.writeToNBT(new CompoundTag()));
		
		
		super.saveAdditional(nbt);
	}
	
	@Override
	public void load(@NotNull CompoundTag nbt) {
		super.load(nbt);
		
		this.linkedClaimUUID = UUID.fromString(nbt.getString("claimUUID"));
		this.linkedClaimFeatureUUID = UUID.fromString(nbt.getString("claimFeatureUUID"));
		this.owningFactionUUID = UUID.fromString(nbt.getString("factionUUID"));
		this.newOwningFactionUUID = UUID.fromString(nbt.getString("newOwningFactionUUID"));
		this.newOwnerUUID = UUID.fromString(nbt.getString("newOwnerUUID"));
		this.owningFactionName = nbt.getString("factionName");
		this.ownerUUID = UUID.fromString(nbt.getString("ownerUUID"));
		this.ownerName = nbt.getString("ownerName");
		this.upgradeAmount = nbt.getInt("upgradeAmount");
		this.color = nbt.getInt("color");
		this.remainingFuel = nbt.getInt("fuel");
		this.claimSize = nbt.getInt("claimSize");
		this.tickCounter = nbt.getInt("tickCounter");
		this.timer = nbt.getInt("timer");
		this.timer2 = nbt.getInt("timer2");
		this.status = nbt.getInt("status");
		this.errorCode = nbt.getInt("errorCode");
		this.hackProgress = nbt.getInt("hackProgress");
		this.hackDecreaseTimer = nbt.getInt("hackDecreaseTimer");
		this.fullyInitialized = nbt.getBoolean("fullyInitialized");
		this.eventTime = new SerializableDateTime().readFromNBT(nbt.getCompound("eventTime"));
		
		this.upgradeData.readFromNBT(nbt.getCompound("upgradeData"));
	}
	
	@Override
	public AABB getRenderBoundingBox() {
		return super.getRenderBoundingBox().inflate(0, Double.POSITIVE_INFINITY, 0);
	}
	
	//////// Getters ////////
	
	public String getOwningFactionName() {
		return owningFactionName;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public int getClaimSize() {
		return claimSize;
	}
	
	public UUID getLinkedClaimUUID() {
		return linkedClaimUUID;
	}
	
	public UUID getLinkedClaimFeatureUUID() {
		return linkedClaimFeatureUUID;
	}
	
	public UUID getOwningFactionUUID() {
		return owningFactionUUID;
	}
	
	public UUID getOwnerUUID() {
		return ownerUUID;
	}
	
	public int getStatus() {
		return status;
	}
	
	public boolean hasFuel() {
		return true;
	}
	
	public int getRemainingFuel() {
		return remainingFuel;
	}
	
	public int getMaxFuel() {
		return 60 * 60 * 8;   //8 hours
	}
	
	public int getTimer() {
		return this.timer;
	}
	
	public int getTimer2() {
		return this.timer2;
	}
	
	public int getHackProgress() {
		return hackProgress;
	}
	
	public SerializableDateTime getEventTime() {
		return eventTime;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public int getColor() {
		return color;
	}
	
	public int getUpgradeAmount() {
		return upgradeAmount;
	}
	
	//////
	
	public static class BeaconHelper {
		
		/*
		 * note: all function takes Player rather than ServerPlayer,
		 * because they may be checked client side.
		 */
		
		/**
		 * get the status that is resulted in when pressing cancel in GUI
		 *
		 * @param status - the current status
		 * @return the status after cancel, -1 if not allowed
		 */
		public static int getCancelToStatus(int status) {
			switch (status) {
				case STATUS_OFF, STATUS_ERRORED_STOPPING, STATUS_UNSTABLE_REPAIRING, STATUS_UNSTABLE_STOPPING -> {
					return -1;
				}
				case STATUS_STARTING, STATUS_CHANGING_FACTION -> {
					return STATUS_OFF;
				}
				case STATUS_STOPPING -> {
					return STATUS_RUNNING;
				}
				case STATUS_UNSTABLE_CHANGING_FACTION -> {
					return STATUS_UNSTABLE_REPAIRING;
				}
			}
			return -1;
		}
		
		public static boolean isRunning(int status) {
			return status == STATUS_RUNNING || status == STATUS_UNSTABLE_RUNNING || status == STATUS_STOPPING;
		}
		
		public static boolean isProperRunning(int status) {
			return status == STATUS_RUNNING || status == STATUS_STOPPING;
		}
		
		public static boolean isProperOff(int status) {
			return status == STATUS_OFF || status == STATUS_ERRORED_OFF;
		}
		
		public static boolean isOff(int status) {
			return isProperOff(status) || status == STATUS_UNSTABLE_REPAIRING;
		}
		
		public static boolean isUnstableState(int status) {
			return status == STATUS_UNSTABLE_RUNNING ||
			       status == STATUS_UNSTABLE_REPAIRING ||
			       status == STATUS_UNSTABLE_STOPPING ||
			       status == STATUS_UNSTABLE_CHANGING_FACTION;
		}
		
		/**
		 * Check if a player is allowed to open the UI of the beacon
		 * When beacon is running, only current faction player with >= owner's permission can use it
		 * When off, everyone can access it
		 *
		 * @param player - the player trying to access
		 * @param beacon - the beacon block
		 * @return Error message if not allowed, or empty if allowed.
		 */
		public static Optional<Component> accessUIAllowed(Player player, ClaimBeaconBlockEntity beacon) {
			if (player.getEyePosition().distanceTo(beacon.getBlockPos().getCenter()) > 6) {
				return Optional.of(Component.translatable("caw.errors.generic.too_far"));
			}
			if (isRunning(beacon.status)) {
				if (!beacon.isOwnerAndFactionValid()) {
					return Optional.empty();    //Shouldn't happen but just in case
				}
				Optional<Component> factionPerm = checkFactionPermission(player, beacon);
				if (factionPerm.isPresent()) return factionPerm;
			}
			return Optional.empty();
		}
		
		private static Optional<Component> checkFactionPermission(Player player, ClaimBeaconBlockEntity beacon) {
			if (!FactionDataManager.get().isPlayerInFaction(player.getUUID(), beacon.getOwningFactionUUID())) {
				return Optional.of(Component.translatable("caw.errors.beacon.not_in_owning_faction", beacon.getOwningFactionName()));
			}
			if (!FactionDataManager.get().isPermissionEqualOrHigher(beacon.getOwnerUUID(), player.getUUID(), beacon.getOwningFactionUUID())) {
				return Optional.of(
				Component.translatable("caw.errors.beacon.lower_permission", beacon.getOwnerName(), beacon.getOwningFactionName())
				);
			}
			return Optional.empty();
		}
		
		public static Optional<Component> allowedToOperate(Player player, ClaimBeaconBlockEntity beacon) {
			if (!beacon.isOwnerAndFactionValid()) return Optional.empty();
			return checkFactionPermission(player, beacon);
		}
		
		public static Optional<Component> allowedToTransfer(Player player, ClaimBeaconBlockEntity beacon) {
			if (isProperOff(beacon.status)) {
				if (FactionDataManager.get()
				    .getPrimaryFaction(player.getUUID())
				    .map(factionData -> factionData.getFactionUUID().equals(beacon.getOwningFactionUUID()))
				    .orElse(false) &&
				    
				    player.getUUID().equals(beacon.getOwnerUUID())
				) {
					return Optional.of(
					Component.translatable("caw.errors.beacon.same_faction_set")
					);
				}
			} else {
				return Optional.of(
				Component.translatable("caw.errors.beacon.must_turn_off")
				);
			}
			return Optional.empty();
		}
		
		public static boolean canBreak(Player player, ClaimBeaconBlockEntity beacon) {
			if (isUnstableState(beacon.status)) return false;
			if (isRunning(beacon.status)) return false;
			if (!beacon.isOwnerAndFactionValid()) {
				return true;
			}
			return FactionDataManager.get().isPermissionEqualOrHigher(beacon.getOwnerUUID(), player.getUUID(), beacon.getOwningFactionUUID());
		}
		
		public static boolean cancelAllowed(Player player, ClaimBeaconBlockEntity beacon) {
			if (!beacon.isOwnerAndFactionValid()) return true;
			return FactionDataManager.get().isPlayerInFaction(player.getUUID(), beacon.getOwningFactionUUID());
		}
	}
	
	public static class BeaconPacketHandler {
		
		public static final int INST_ON = 1;
		public static final int INST_OFF = 2;
		public static final int INST_CHANGE_FACTION = 3;
		public static final int INST_CANCEL = -1;
		
		public static void handleInstruction(ServerPlayer player, int instruction) {
			if (player == null) return;
			if (!(player.containerMenu instanceof ClaimBeaconMenu menu)) return;
			ClaimBeaconBlockEntity block = menu.block;
			
			if (!genericCheck(player, block)) return;
			
			switch (instruction) {
				case INST_ON -> handleOn(player, block);
				case INST_OFF -> handleOff(player, block);
				case INST_CHANGE_FACTION -> handleChangeFaction(player, block);
				case INST_CANCEL -> handleCancel(player, block);
				default -> {
					player.sendSystemMessage(Component.literal("Invalid instruction!"));
					CAWConstants.LOGGER.error("Unknown instruction received from player {} for beacon {}: {}",
					player.getScoreboardName(),
					block.getBlockPos(),
					new IllegalArgumentException("Unknown state " + instruction + "!"));
				}
			}
			block.refreshName();
			block.sendData();
		}
		
		private static boolean genericCheck(ServerPlayer player, ClaimBeaconBlockEntity block) {
			Optional<Component> error = BeaconHelper.accessUIAllowed(player, block);
			if (error.isPresent()) {
				player.sendSystemMessage(error.get());
				return false;
			}
			if (!block.isOwnerAndFactionValid()) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.not_set_properly"));
				return false;
			}
			return true;
		}
		
		private static void handleOn(ServerPlayer player, ClaimBeaconBlockEntity block) {
			if (!BeaconHelper.isProperOff(block.status)) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.not_off"));
				return;
			}
			if (!block.hasFuel()) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.no_fuel"));
				return;
			}
			block.status = STATUS_STARTING;
			block.timer = block.getTransientTime(STATUS_STARTING);
		}
		
		private static void handleOff(ServerPlayer player, ClaimBeaconBlockEntity block) {
			if (!BeaconHelper.isProperRunning(block.getStatus())) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.not_on"));
				return;
			}
			block.status = STATUS_STOPPING;
			block.timer = block.getTransientTime(STATUS_STOPPING);
		}
		
		private static void handleChangeFaction(ServerPlayer player, ClaimBeaconBlockEntity block) {
			if (!BeaconHelper.isOff(block.status)) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.must_turn_off"));
				return;
			}
			UUID playerUUID = player.getUUID();
			Optional<UUID> factionUUID = FactionDataManager.get().getPrimaryFaction(playerUUID).map(FactionData::getFactionUUID);
			if (factionUUID.isEmpty()) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.no_primary_faction"));
				return;
			}
			if (BeaconHelper.isUnstableState(block.status)) {
				if (block.owningFactionUUID.equals(factionUUID.get())) {
					player.sendSystemMessage(Component.translatable("caw.errors.beacon.same_faction_set"));
					return;
				}
				if (FactionDataManager.get().isPlayerInFaction(playerUUID, block.owningFactionUUID)) {
					player.sendSystemMessage(Component.translatable("caw.errors.beacon.do_not_circumvent_system"));
					return;
				}
			}
			block.newOwnerUUID = playerUUID;
			block.newOwningFactionUUID = factionUUID.get();
			if (BeaconHelper.isUnstableState(block.status)) {
				block.status = STATUS_UNSTABLE_CHANGING_FACTION;
				block.timer2 = block.getTransientTime(STATUS_UNSTABLE_CHANGING_FACTION);
			} else {
				block.status = STATUS_CHANGING_FACTION;
				block.timer = block.getTransientTime(STATUS_CHANGING_FACTION);
			}
		}
		
		@SuppressWarnings("DuplicateBranchesInSwitch")
		private static void handleCancel(ServerPlayer player, ClaimBeaconBlockEntity block) {
			if (!BeaconHelper.cancelAllowed(player, block)) {
				player.sendSystemMessage(Component.translatable("caw.errors.beacon.not_in_owning_faction"));
			}
			switch (block.status) {
				case STATUS_STARTING -> {
					block.status = STATUS_OFF;
					block.timer = 0;
				}
				case STATUS_STOPPING -> {
					block.status = STATUS_RUNNING;
					block.timer = 0;
				}
				case STATUS_UNSTABLE_CHANGING_FACTION -> {
					block.status = STATUS_UNSTABLE_REPAIRING;
					block.timer2 = 0;
				}
				case STATUS_CHANGING_FACTION -> {
					block.status = STATUS_OFF;
					block.timer = 0;
				}
				default -> {
					player.sendSystemMessage(Component.translatable("caw.errors.beacon.can_not_cancel"));
				}
			}
		}
	}
}

// NO-OP container data because sync is done through sending NBT
class ClaimBeaconContainerData implements ContainerData {
	
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
}