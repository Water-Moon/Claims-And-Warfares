package net.rainfantasy.claims_and_warfares.common.game_objs.blocks.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {
	
	public AbstractMachineBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}
	
	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public @NotNull CompoundTag getUpdateTag() {
		return this.writeSynced(new CompoundTag());
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag nbt = pkt.getTag();
		this.readSynced(nbt);
	}
	
	public abstract void tick(Level level, BlockPos pos, BlockState state);
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.readSynced(tag);
	}
	
	public void sendData() {
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().blockChanged(getBlockPos());
		}
	}
	
	public void refresh() {
		this.setChanged();
		this.sendData();
	}
	
	public void readSynced(CompoundTag nbt) {
		this.load(nbt);
	}
	
	public CompoundTag writeSynced(CompoundTag nbt) {
		this.saveAdditional(nbt);
		return nbt;
	}
	
	public void openScreen(ServerPlayer player) {
		NetworkHooks.openScreen(player, this, byteBuf -> {
			byteBuf.writeBlockPos(this.getBlockPos());
			byteBuf.writeNbt(this.getUpdateTag());
		});
	}
}
