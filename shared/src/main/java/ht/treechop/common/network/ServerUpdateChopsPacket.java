package ht.treechop.common.network;

import ht.treechop.TreeChop;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerUpdateChopsPacket implements CustomPacket {
    public static final ResourceLocation ID = TreeChop.resource("server_update_chops");
    private static Level lastLevel = null;

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

    public FriendlyByteBuf encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(tag);
        return buffer;
    }

    public static ServerUpdateChopsPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        return new ServerUpdateChopsPacket(pos, tag);
    }

    public static void handle(ServerUpdateChopsPacket message) {
        pendingUpdates.put(message.pos, message.tag);
    }

    private static void checkLevel(Level level) {
        if (level != lastLevel) {
            pendingUpdates.clear();
            lastLevel = level;
        }
    }

    public static CompoundTag getPendingUpdate(Level level, BlockPos pos) {
        checkLevel(level);
        return pendingUpdates.remove(pos);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BlockPos getPos() {
        return pos;
    }

    public CompoundTag getTag() {
        return tag;
    }
}
