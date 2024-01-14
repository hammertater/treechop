package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerUpdateChopsPacket implements CustomPacket {
    public static final ResourceLocation ID = TreeChop.resource("server_update_chops");

    private final BlockPos pos;
    private final CompoundTag tag;

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
        Client.handleUpdateChopsPacket(message.pos, message.tag);
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
