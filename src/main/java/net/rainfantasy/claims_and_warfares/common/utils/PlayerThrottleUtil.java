package net.rainfantasy.claims_and_warfares.common.utils;

import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PlayerThrottleUtil {
	private static final HashMap<UUID, Long> lastOperationTimeMillis = new HashMap<>();
	private static final long OPERATION_COOLDOWN_MS = 1000;
	
	public static boolean throttlePlayerOperation(UUID playerUUID){
		long currentTime = System.currentTimeMillis();
		long lastOperationTime = lastOperationTimeMillis.getOrDefault(playerUUID, 0L);
		if(currentTime - lastOperationTime < OPERATION_COOLDOWN_MS){
			return true;
		}
		lastOperationTimeMillis.put(playerUUID, currentTime);
		return false;
	}
	
	public static Component getErrorMessage(){
		return Component.translatable("caw.errors.generic.too_fast");
	}
}
