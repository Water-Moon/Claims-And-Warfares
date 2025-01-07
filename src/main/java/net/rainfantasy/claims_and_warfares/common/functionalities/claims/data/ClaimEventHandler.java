package net.rainfantasy.claims_and_warfares.common.functionalities.claims.data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class ClaimEventHandler {
	
	private static long lastTickTime = 0;
	private static final long CHECK_INTERVAL = 1000;
	
	public static void tick() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastTickTime < CHECK_INTERVAL) return;
		lastTickTime = currentTime;
		
		ClaimDataManager claimDataManager = ClaimDataManager.get();
		List<UUID> claimsToRemove = new ArrayList<>();
		synchronized (claimDataManager) {
			for (UUID claimUUID : claimDataManager.getAllClaimUUIDs()) {
				claimDataManager.getClaim(claimUUID).ifPresent(data -> {
					if (data.isInvalid()) {
						claimsToRemove.add(claimUUID);
						CAWConstants.debugLog("Removing invalidated claim {}", claimUUID);
					}
				});
			}
			for (UUID claimUUID : claimsToRemove) {
				claimDataManager.removeClaim(claimUUID);
			}
		}
		
	}
	
	public static void onBlockBreak(BreakEvent event) {
		BlockPos pos = event.getPos();
		ClaimDataManager.get().getClaimsAt(event.getLevel(), pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onBreakBlock(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	public static void onBlockPlace(EntityPlaceEvent event) {
		BlockPos pos = event.getPos();
		ClaimDataManager.get().getClaimsAt(event.getLevel(), pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onPlaceBlock(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	public static void onInteract(RightClickBlock event) {
		BlockPos pos = event.getPos();
		ClaimDataManager.get().getClaimsAt(event.getLevel(), pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onInteractBlock(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	public static void onFarmlandTrample(FarmlandTrampleEvent event) {
		BlockPos pos = event.getPos();
		ClaimDataManager.get().getClaimsAt(event.getLevel(), pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onFarmlandTrample(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	public static void onAttack(LivingAttackEvent event) {
		if (event.getEntity().getCommandSenderWorld().isClientSide()) return;
		Level level = event.getEntity().level();
		BlockPos pos = event.getEntity().blockPosition();
		Optional<BlockPos> pos2 = Optional.ofNullable(event.getSource().getEntity()).map(Entity::blockPosition);
		Stream.concat(
		ClaimDataManager.get().getClaimsAt(event.getEntity().level(), pos).stream(),
		pos2.map(aPos -> ClaimDataManager.get().getClaimsAt(level, aPos)).stream().flatMap(Collection::stream)
		).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onEntityAttacked(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	public static void onExplosion(Detonate event) {
		Set<Vector2i> chunkPos = new HashSet<>();
		event.getAffectedBlocks().forEach(pos -> chunkPos.add(CoordUtil.blockToChunk(pos)));
		
		chunkPos.forEach(pos -> {
			ClaimDataManager.get().getClaimsAt(event.getLevel(), pos).forEach(data -> {
				data.claimFeatures.forEach(feature -> {
					feature.onExplosion(event);
				});
			});
		});
	}
	
	public static void onMobGriefing(EntityMobGriefingEvent event) {
		Level level = event.getEntity().level();
		BlockPos pos = event.getEntity().getOnPos();
		ClaimDataManager.get().getClaimsAt(level, pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				if (!feature.onMobGriefing(event)) {
					event.setCanceled(true);
				}
			});
		});
	}
	
	//claim -> list of players
	private static final HashMap<UUID, CopyOnWriteArraySet<UUID>> playersInClaims = new HashMap<>();
	private static long lastEnterExitCheckTime = 0;
	private static final long ENTER_EXIT_CHECK_INTERVAL = 1000;
	
	static void tickEnterExit(ServerTickEvent event) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastEnterExitCheckTime < ENTER_EXIT_CHECK_INTERVAL) return;
		lastEnterExitCheckTime = currentTime;
		
		event.getServer().getPlayerList().getPlayers().forEach(player -> {
			if (ClaimDataManager.get().canPlayerBypass(player.getUUID())) return;
			Vec3 pos = player.getPosition(1);
			Stream.concat(
			ClaimDataManager.get().getClaimsAt(player.level(), CoordUtil.blockToChunk(pos)).stream(),
			playersInClaims.keySet().stream()
			.map(ClaimDataManager.get()::getClaim)
			.flatMap(Optional::stream)
			).toList()  //NOTE: not redundant, otherwise ForEach is lazy and may result in ConcurrentModificationException
			.forEach(claim -> handlePlayerEnterExit(claim, player));
		});
	}
	
	private static void handlePlayerEnterExit(ClaimData claim, ServerPlayer player) {
		UUID playerUUID = player.getUUID();
		if (claim.isEntityIn(player)) {
			if (!playersInClaims.computeIfAbsent(claim.getUUID(), k -> new CopyOnWriteArraySet<>()).contains(playerUUID)) {
				playersInClaims.get(claim.getUUID()).add(playerUUID);
				claim.claimFeatures.forEach(feature -> {
					feature.onPlayerEnterClaim(player);
				});
			}
		} else {
			if (playersInClaims.computeIfAbsent(claim.getUUID(), k -> new CopyOnWriteArraySet<>()).contains(playerUUID)) {
				playersInClaims.get(claim.getUUID()).remove(playerUUID);
				claim.claimFeatures.forEach(feature -> {
					feature.onPlayerLeaveClaim(player);
				});
			}
		}
	}
	
	public static void fireExitEventOnUnClaim(ClaimData data, MinecraftServer server) {
		server.getPlayerList().getPlayers().forEach(player -> {
			if (playersInClaims.computeIfAbsent(data.getUUID(), k -> new CopyOnWriteArraySet<>()).contains(player.getUUID())) {
				playersInClaims.get(data.getUUID()).remove(player.getUUID());
				data.claimFeatures.forEach(feature -> {
					feature.onPlayerLeaveClaim(player);
				});
			}
		});
		playersInClaims.remove(data.getUUID());
	}
	
	public static void fireExitEventForPlayerCommon(ServerPlayer player) {
		playersInClaims.keySet().forEach(claimUUID -> {
			ClaimDataManager.get().getClaim(claimUUID).ifPresent(claim -> {
				UUID playerUUID = player.getUUID();
				if (playersInClaims.computeIfAbsent(claim.getUUID(), k -> new CopyOnWriteArraySet<>()).contains(playerUUID)) {
					playersInClaims.get(claim.getUUID()).remove(playerUUID);
					claim.claimFeatures.forEach(feature -> {
						feature.onPlayerLeaveClaim(player);
					});
				}
			});
		});
	}
	
	public static void fireExitEventOnLogout(ServerPlayer player) {
		fireExitEventForPlayerCommon(player);
	}
	
	public static void fireExitEventOnBypassEnable(ServerPlayer player) {
		fireExitEventForPlayerCommon(player);
	}
	
	public static void onEntityTick(LivingTickEvent event) {
		if (event.getEntity().getCommandSenderWorld().isClientSide) return;
		
		BlockPos pos = event.getEntity().blockPosition();
		
		ClaimDataManager.get().getClaimsAt(event.getEntity().level(), pos).forEach(data -> {
			data.claimFeatures.forEach(feature -> {
				feature.onEntityTickInsideClaim(event);
			});
		});
		
	}
}

/**
 * Event listener for claim events. <br>
 * All @SubscribeEvent methods should be placed here
 * and call respective methods in the ClaimEventHandler
 */
@EventBusSubscriber(modid = CAWConstants.MODID, bus = EventBusSubscriber.Bus.FORGE)
class ClaimEventListener {
	
	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		ClaimEventHandler.tick();
		ClaimEventHandler.tickEnterExit(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak(BreakEvent event) {
		if (event.getLevel().isClientSide()) return;
		if (event.getPlayer() != null && ClaimDataManager.get().canPlayerBypass(event.getPlayer().getUUID())) return;
		ClaimEventHandler.onBlockBreak(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(EntityPlaceEvent event) {
		if (event.getLevel().isClientSide()) return;
		if (event.getEntity() != null && ClaimDataManager.get().canPlayerBypass(event.getEntity().getUUID())) return;
		ClaimEventHandler.onBlockPlace(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onInteract(RightClickBlock event) {
		if (event.getLevel().isClientSide()) return;
		if ((event.getEntity() instanceof ServerPlayer player) && ClaimDataManager.get().canPlayerBypass(player.getUUID()))
			return;
		ClaimEventHandler.onInteract(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onFarmlandTrample(FarmlandTrampleEvent event) {
		if (event.getLevel().isClientSide()) return;
		if ((event.getEntity() instanceof ServerPlayer player) && ClaimDataManager.get().canPlayerBypass(player.getUUID()))
			return;
		ClaimEventHandler.onFarmlandTrample(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onExplosion(Detonate event) {
		if (event.getLevel().isClientSide()) return;
		ClaimEventHandler.onExplosion(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityTick(LivingTickEvent event) {
		if (event.getEntity().getCommandSenderWorld().isClientSide()) return;
		if ((event.getEntity() instanceof ServerPlayer player) && ClaimDataManager.get().canPlayerBypass(player.getUUID()))
			return;
		ClaimEventHandler.onEntityTick(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onAttack(LivingAttackEvent event) {
		if (event.getEntity().getCommandSenderWorld().isClientSide()) return;
		if ((event.getEntity() instanceof ServerPlayer player) && ClaimDataManager.get().canPlayerBypass(player.getUUID()))
			return;
		ClaimEventHandler.onAttack(event);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPlayerLogout(PlayerLoggedOutEvent event) {
		if (event.getEntity().getCommandSenderWorld().isClientSide()) return;
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
		ClaimEventHandler.fireExitEventOnLogout(serverPlayer);
	}
}