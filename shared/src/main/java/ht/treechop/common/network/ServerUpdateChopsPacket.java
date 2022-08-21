package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerUpdateChopsPacket implements CustomPacket {
    private static final ResourceLocation id = TreeChop.resource("server_update_chops");
    private final BlockPos pos;
    private final CompoundTag tag;

    private static final LinkedHashMap<BlockPos, CompoundTag> pendingUpdates = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, CompoundTag> eldest) {
            return size() > 16;
        }
    };

    public ServerUpdateChopsPacket(BlockPos pos, CompoundTag tag) {
        this.pos = pos;
        this.tag = tag;
    }

    public static void encode(ServerUpdateChopsPacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeNbt(message.tag);
    }

    public static ServerUpdateChopsPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        return new ServerUpdateChopsPacket(pos, tag);
    }

    public static void handle(ServerUpdateChopsPacket message, ServerPlayer sender) {
        pendingUpdates.put(message.pos, message.tag);
    }

    public static void update(Level level, BlockPos pos) {
        CompoundTag tag = pendingUpdates.get(pos);
        if (tag != null && level != null && level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            entity.load(tag);
            level.setBlocksDirty(pos, Blocks.AIR.defaultBlockState(), level.getBlockState(pos));
        }
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

}
