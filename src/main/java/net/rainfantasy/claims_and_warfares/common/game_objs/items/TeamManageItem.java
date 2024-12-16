package net.rainfantasy.claims_and_warfares.common.game_objs.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rainfantasy.claims_and_warfares.common.functionalities.factions.networking.FactionPacketGenerator;
import net.rainfantasy.claims_and_warfares.common.setups.networking.faction.menu.PTCOpenFactionManagePacket;
import net.rainfantasy.claims_and_warfares.common.setups.registries.ChannelRegistry;
import org.jetbrains.annotations.NotNull;

public class TeamManageItem extends Item {
	
	public TeamManageItem() {
		super(new Properties().stacksTo(1));
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		
		if (player instanceof ServerPlayer serverPlayer) {
			ChannelRegistry.sendToClient(serverPlayer, new PTCOpenFactionManagePacket());
			FactionPacketGenerator.scheduleSend(serverPlayer);
		}
		
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
