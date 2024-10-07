package ht.treechop.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacket extends CustomPacketPayload {
    ResourceLocation getId();

    void encode(FriendlyByteBuf buffer);
}
