package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

import java.util.Optional;

public interface SimpleCodec<T> {

    T decode(PacketBuffer buffer);

    void encode(PacketBuffer buffer, Object value);

    String getLocalizationString(Object object);

    Optional<T> getValueOf(Object object);

}
