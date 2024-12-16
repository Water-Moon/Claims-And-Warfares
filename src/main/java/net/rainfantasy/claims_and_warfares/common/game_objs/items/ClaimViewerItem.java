package net.rainfantasy.claims_and_warfares.common.game_objs.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rainfantasy.claims_and_warfares.common.functionalities.claims.networking.ClaimPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.functionalities.mapper.MapPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.claim.PTCOpenViewerPacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

public class ClaimViewerItem extends Item {
	
	public ClaimViewerItem() {
		super(new Properties().stacksTo(1));
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		
		if (player instanceof ServerPlayer serverPlayer) {
			// open the screen
			ChannelRegistry.sendToClient(serverPlayer, new PTCOpenViewerPacket(5, 5));
			//send map info
			MapPacketGenerator.scheduleSend(serverPlayer, 5, 5);
			//send claim info
			ClaimPacketGenerator.scheduleSend(serverPlayer, 5, 5);
		}
		
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
