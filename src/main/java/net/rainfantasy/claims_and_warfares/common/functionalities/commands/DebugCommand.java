package net.rainfantasy.claims_and_warfares.common.functionalities.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;

public class DebugCommand {
	
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(createDebugCommand());
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> createDebugCommand() {
		return Commands.literal("caw_debug")
		       .then(addDebugClaimCommand())
		       .then(removeDebugClaimCommand())
		       .then(debugListFactionsCommand())
		;
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> addDebugClaimCommand() {
		return Commands.literal("claim")
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
					       BlockPos pos = player.blockPosition();
					       ClaimDataManager.get().debugAddClaimAt(player.level(), pos);
				       }
			       } catch (Exception ex) {
				       source.sendFailure(Component.literal(ex.getMessage()));
				       for (StackTraceElement element : ex.getStackTrace()) {
					       source.sendFailure(Component.literal(element.toString()));
				       }
			       }
			       return 1;
		       })
		;
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> removeDebugClaimCommand() {
		return Commands.literal("unclaim")
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
					       BlockPos pos = player.blockPosition();
					       ClaimDataManager.get().debugRemoveClaimAt(player.level(), pos);
				       }
			       } catch (Exception ex) {
				       source.sendFailure(Component.literal(ex.getMessage()));
				       for (StackTraceElement element : ex.getStackTrace()) {
					       source.sendFailure(Component.literal(element.toString()));
				       }
			       }
			       return 1;
		       })
		;
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> debugListFactionsCommand() {
		return Commands.literal("list_factions")
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
					       FactionDataManager.get().getAllFactions().forEach(faction -> {
						       source.sendSuccess(() -> Component.literal(faction.toString()), true);
					       });
				       }
			       } catch (Exception ex) {
				       source.sendFailure(Component.literal(ex.getMessage()));
				       for (StackTraceElement element : ex.getStackTrace()) {
					       source.sendFailure(Component.literal(element.toString()));
				       }
			       }
			       return 1;
		       })
		;
	}
}
