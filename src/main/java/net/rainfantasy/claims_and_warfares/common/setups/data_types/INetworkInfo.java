package net.rainfantasy.claims_and_warfares.common.setups.data_types;

import net.minecraft.network.FriendlyByteBuf;

public interface INetworkInfo<C> {
	
	void toBytes(FriendlyByteBuf byteBuf);
	
	C fromBytes(FriendlyByteBuf byteBuf);
}
