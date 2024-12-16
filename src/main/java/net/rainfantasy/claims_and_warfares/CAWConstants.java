package net.rainfantasy.claims_and_warfares;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

public class CAWConstants {
	
	public static final String MODID = "claims_and_warfares";
	public static final Logger LOGGER = LoggerFactory.getLogger("CAW Main");
	public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("CAW Debug");
	
	public static final UUID NIL_UUID = new UUID(0, 0);
	
	private static final boolean isProduction = FMLLoader.isProduction();
	
	private static MinecraftServer server;
	
	@Contract("_ -> new")
	public static @NotNull ResourceLocation rl(@NotNull String id) {
		return new ResourceLocation(CAWConstants.MODID, id);
	}
	
	public static void debugLog(String content){
		if(!isProduction){
			DEBUG_LOGGER.info(content);
		}
	}
	
	public static void debugLog(String content, Object ... objects){
		if(!isProduction){
			DEBUG_LOGGER.info(content, objects);
		}
	}
	
	protected static void setServer(MinecraftServer server) {
		CAWConstants.server = server;
	}
	
	private static int errorCount = 0;
	public static boolean execute(@NotNull Runnable r){
		try {
			if (server != null) {
				server.executeIfPossible(r);
			} else {
				Minecraft.getInstance().executeIfPossible(r);
			}
			return true;
		}catch (RejectedExecutionException e){
			LOGGER.info("Failed to execute task (rejected): {}", r.toString());
			LOGGER.info("Error (rejected): ", e);
			LOGGER.info("This can be safely ignored - these are probably just long-running tasks.");
			return false;
		}catch (Exception e){
			LOGGER.error("Failed to execute task (error #{}): {}", ++errorCount, r.toString());
			LOGGER.error("Error #{}: ", errorCount, e);
			return false;
		}
	}
	
	public static void logStackTrace() {
		LOGGER.info("Stack trace:", new Exception());
	}
}
