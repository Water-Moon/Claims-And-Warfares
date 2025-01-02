package net.rainfantasy.claims_and_warfares;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.rainfantasy.claims_and_warfares.client.gui.BeaconHackerScreen;
import net.rainfantasy.claims_and_warfares.client.gui.ClaimBeaconScreen;
import net.rainfantasy.claims_and_warfares.client.render.block.BeaconHackerRenderer;
import net.rainfantasy.claims_and_warfares.client.render.block.ClaimBeaconRenderer;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.commands.DebugCommand;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.misc.OfflinePlayerDatabase;
import net.rainfantasy.claims_and_warfares.common.setups.registries.*;

@Mod(CAWConstants.MODID)
@EventBusSubscriber(modid = CAWConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClaimsAndWarfares {
	
	public ClaimsAndWarfares() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		BlockRegistry.register(modEventBus);
		ItemRegistry.register(modEventBus);
		BlockEntityRegistry.register(modEventBus);
		MenuRegistry.register(modEventBus);
		RecipeRegistry.register(modEventBus);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::addCreative);
	}
	
	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
		ChannelRegistry.setup();
	}
	
	public void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
			event.accept(ItemRegistry.CLAIM_VIEWER.get());
			event.accept(ItemRegistry.TEAM_MANAGER.get());
		}
		if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
			event.accept(BlockRegistry.CLAIM_BEACON.get());
			event.accept(BlockRegistry.BEACON_UPGRADE_SIZE.get());
			event.accept(BlockRegistry.BEACON_UPGRADE_INTERACT_PROTECTION.get());
			event.accept(BlockRegistry.BEACON_UPGRADE_MOB_GRIEFING.get());
			event.accept(BlockRegistry.BEACON_UPGRADE_EXPLOSION_PROTECTION.get());
			event.accept(BlockRegistry.BEACON_HACKER.get());
		}
	}
	
	@SubscribeEvent
	public void onRegisterCommand(RegisterCommandsEvent event) {
		DebugCommand.register(event);
	}
	
	@SubscribeEvent
	public void onServerStart(ServerStartingEvent event) {
		CAWConstants.LOGGER.info("Server starting");
		CAWConstants.setServer(event.getServer());
		
		OfflinePlayerDatabase.init(event.getServer());
		ClaimDataManager.init(event.getServer());
		FactionDataManager.init(event.getServer());
		FactionClaimDataManager.init(event.getServer());
	}
	
	@SubscribeEvent
	public void onServerStop(ServerStoppedEvent event) {
		CAWConstants.LOGGER.info("Server stopping");
		CAWConstants.setServer(null);
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		CAWConstants.execute(() -> {
			OfflinePlayerDatabase.get().updateData(player);
			FactionPacketGenerator.scheduleSend(player);
			player.sendSystemMessage(Component.translatable("caw.message.motd").withStyle(ChatFormatting.GOLD));
		});
	}
}

@EventBusSubscriber(modid = CAWConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class CAWClient {
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(MenuRegistry.CLAIM_BEACON_MENU.get(), ClaimBeaconScreen::new);
		MenuScreens.register(MenuRegistry.BEACON_HACKER_MENU.get(), BeaconHackerScreen::new);
	}
	
	@SubscribeEvent
	public static void onRegisterBlockRenderer(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(BlockEntityRegistry.CLAIM_BEACON_BE.get(), ClaimBeaconRenderer::new);
		event.registerBlockEntityRenderer(BlockEntityRegistry.BEACON_HACKER_BE.get(), BeaconHackerRenderer::new);
	}
}