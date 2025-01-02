package net.rainfantasy.claims_and_warfares.common.functionalities.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimData;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.data.ClaimDataManager;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.ColoredClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.SystemProtectedClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.features.impl.UnprotectedSystemClaimFeature;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.data.FactionDataManager;
import net.rainfantasy.claims_and_warfares.common.utils.ColorUtil;
import net.rainfantasy.claims_and_warfares.common.utils.CoordUtil;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.UUID;

public class DebugCommand {
	
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(createDebugCommand());
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> createDebugCommand() {
		return Commands.literal("caw_debug")
		       .requires(source -> source.hasPermission(2))
		       .then(addDebugClaimCommand())
		       .then(removeDebugClaimCommand())
		       .then(debugListFactionsCommand())
		       .then(systemClaimCommand())
		       .then(systemUnclaimCommand())
		       .then(toggleBypassCommand())
		;
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> addDebugClaimCommand() {
		return Commands.literal("debug_claim")
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
		return Commands.literal("debug_unclaim")
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
	
	public static LiteralArgumentBuilder<CommandSourceStack> systemClaimCommand() {
		return Commands.literal("claim")
		       .then(Commands.argument("size", IntegerArgumentType.integer(1))
		             .then(Commands.argument("full_protection", BoolArgumentType.bool())
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
					       BlockPos pos = player.blockPosition();
					       Vector2i chunkPos = CoordUtil.blockToChunk(pos);
						   int size = e.getArgument("size", Integer.class);
							
							ClaimData claim = new ClaimData(source.getLevel());
							Vector2i actualPos = new Vector2i(size - 1);
							CoordUtil.iterateCoords(chunkPos.sub(actualPos, new Vector2i()), chunkPos.add(actualPos, new Vector2i()))
							.forEach(claim::claimChunk);
							
							boolean fullProtection = e.getArgument("full_protection", Boolean.class);
							if(fullProtection) {
								claim.addClaimFeature(new SystemProtectedClaimFeature());
								claim.addClaimFeature(new ColoredClaimFeature(ColorUtil.combine(255, 255, 255, 255)));
							}else {
								claim.addClaimFeature(new UnprotectedSystemClaimFeature());
								claim.addClaimFeature(new ColoredClaimFeature(ColorUtil.combine(255, 255, 255, 128)));
							}
							
							ClaimDataManager.get().addClaim(claim);
							
							source.sendSuccess(() -> Component.translatable("caw.command.success.claim"), true);
				       }
			       } catch (Exception ex) {
				       source.sendFailure(Component.literal(ex.getMessage()));
				       for (StackTraceElement element : ex.getStackTrace()) {
					       source.sendFailure(Component.literal(element.toString()));
				       }
			       }
			       return 1;
		       })));
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> systemUnclaimCommand() {
		return Commands.literal("unclaim")
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
					       BlockPos pos = player.blockPosition();
					       Vector2i chunkPos = CoordUtil.blockToChunk(pos);
						   HashSet<UUID> claimsToRemove = new HashSet<>();
					       ClaimDataManager.get().getClaimsAt(player.level(), chunkPos).forEach(claim -> {
							   if(claim.hasFeature(SystemProtectedClaimFeature.class) || claim.hasFeature(UnprotectedSystemClaimFeature.class)) {
								   claimsToRemove.add(claim.getUUID());
							   }
					       });
						   
						   claimsToRemove.forEach(claimUUID -> ClaimDataManager.get().removeClaim(claimUUID));
						   
						   source.sendSuccess(() -> Component.translatable("caw.command.success.unclaim"), true);
				       }
			       } catch (Exception ex) {
				       source.sendFailure(Component.literal(ex.getMessage()));
				       for (StackTraceElement element : ex.getStackTrace()) {
					       source.sendFailure(Component.literal(element.toString()));
				       }
			       }
			       return 1;
		       });
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
	
	public static LiteralArgumentBuilder<CommandSourceStack> toggleBypassCommand(){
		return Commands.literal("toggle_bypass")
		       .executes(e -> {
			       CommandSourceStack source = e.getSource();
			       try {
				       if (source.isPlayer()) {
					       ServerPlayer player = source.getPlayer();
					       assert player != null;
						   UUID playerUUID = player.getUUID();
						   if(ClaimDataManager.get().canPlayerBypass(playerUUID)){
							   ClaimDataManager.get().removeBypassPlayer(playerUUID);
							   source.sendSuccess(() -> Component.translatable("caw.command.success.bypass_off"), true);
						   } else {
							   ClaimDataManager.get().addBypassPlayer(player);
							   source.sendSuccess(() -> Component.translatable("caw.command.success.bypass_on"), true);
						   }
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
