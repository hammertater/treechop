package ht.treechop.common.network;

import ht.treechop.common.block.ForgeChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ServerChoppedLogPreparedUpdate {
    private final CompoundTag updateTag;
    private final BlockPos pos;

    private static final LinkedHashMap<BlockPos, CompoundTag> pendingUpdates = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, CompoundTag> eldest) {
            return size() > 16;
        }
    };

    public ServerChoppedLogPreparedUpdate(BlockPos pos, final CompoundTag updateTag) {
        this.pos = pos;
        this.updateTag = updateTag;
    }

    public static void encode(ServerChoppedLogPreparedUpdate message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeNbt(message.updateTag);
    }

    public static ServerChoppedLogPreparedUpdate decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag updateTag = buffer.readNbt();
        return new ServerChoppedLogPreparedUpdate(pos, updateTag);
    }

    public static void handle(ServerChoppedLogPreparedUpdate message, Supplier<NetworkEvent.Context> context) {
        if (!context.get().getDirection().getReceptionSide().isServer()) {
            pendingUpdates.put(message.pos, message.updateTag);
        }
        context.get().setPacketHandled(true);
    }

    public static void update(Level level, BlockPos pos) {
        CompoundTag updateTag = pendingUpdates.get(pos);
        if (level != null && updateTag != null && level.getBlockEntity(pos) instanceof ForgeChoppedLogBlock.MyEntity entity) {
            entity.handleUpdateTag(updateTag);
            level.setBlocksDirty(pos, Blocks.AIR.defaultBlockState(), level.getBlockState(pos));
        }
    }
}
