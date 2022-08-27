package ht.treechop.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacket {
    ResourceLocation getId();

    FriendlyByteBuf encode(FriendlyByteBuf buffer);
}
