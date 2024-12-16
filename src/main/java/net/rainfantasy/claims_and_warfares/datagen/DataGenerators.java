package net.rainfantasy.claims_and_warfares.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.rainfantasy.claims_and_warfares.CAWConstants;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CAWConstants.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
	
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
		
		generator.addProvider(event.includeServer(), LootTableData.create(packOutput));
	}
}
