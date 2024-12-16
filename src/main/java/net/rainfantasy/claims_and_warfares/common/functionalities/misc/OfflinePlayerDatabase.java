package net.rainfantasy.claims_and_warfares.common.functionalities.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class OfflinePlayerDatabase extends SavedData {
	
	Map<UUID, String> knownPlayers = new ConcurrentHashMap<UUID, String>();
	private static OfflinePlayerDatabase INSTANCE;
	static MinecraftServer SERVER;
	
	public static void init(MinecraftServer server) {
		if (server == null) return;
		SERVER = server;
		INSTANCE = loadOrCreate(server);
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		if (INSTANCE == null) return;
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			get().updateData(serverPlayer);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogout(PlayerLoggedInEvent event) {
		if (INSTANCE == null) return;
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			get().updateData(serverPlayer);
		}
	}
	
	public static OfflinePlayerDatabase get() {
		return INSTANCE;
	}
	
	public void updateData(ServerPlayer player) {
		if ((!this.knownPlayers.containsKey(player.getUUID())) || (!this.knownPlayers.get(player.getUUID()).equals(player.getScoreboardName()))) {
			this.knownPlayers.put(player.getUUID(), player.getScoreboardName());
			this.setDirty();
		}
	}
	
	public Optional<String> getName(UUID playerUUID) {
		Optional.ofNullable(SERVER.getPlayerList().getPlayer(playerUUID)).ifPresent(this::updateData);
		return Optional.ofNullable(this.knownPlayers.get(playerUUID));
	}
	
	private OfflinePlayerDatabase() {
		this.setDirty();
	}
	
	@Override
	public @NotNull CompoundTag save(CompoundTag nbt) {
		ListTag dataTag = new ListTag();
		this.knownPlayers.forEach((uuid, name) -> {
			CompoundTag entry = new CompoundTag();
			entry.putString("uuid", uuid.toString());
			entry.putString("name", name);
			dataTag.add(entry);
		});
		nbt.put("data", dataTag);
		return nbt;
	}
	
	public static OfflinePlayerDatabase load(CompoundTag nbt) {
		OfflinePlayerDatabase data = new OfflinePlayerDatabase();
		ListTag list = nbt.getList("data", Tag.TAG_COMPOUND);
		list.forEach(tag -> {
			UUID uuid = UUID.fromString(((CompoundTag) tag).getString("uuid"));
			String name = ((CompoundTag) tag).getString("name");
			data.knownPlayers.put(uuid, name);
		});
		return data;
	}
	
	private static OfflinePlayerDatabase loadOrCreate(MinecraftServer server) {
		if (server == null) return new OfflinePlayerDatabase();
		ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
		assert overworld != null;
		CAWConstants.LOGGER.info("Loading factions data");
		return overworld.getDataStorage().computeIfAbsent(OfflinePlayerDatabase::load, OfflinePlayerDatabase::new, "caw_offline_players");
	}
}
