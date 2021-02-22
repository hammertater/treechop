package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

public interface SimpleCodec<T> {

    T decode(PacketBuffer buffer);

    void encode(PacketBuffer buffer, Object value);

}
