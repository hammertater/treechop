package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

import java.util.Optional;
import java.util.Set;

public interface SimpleCodec<T> {

    T decode(PacketBuffer buffer);

    void encode(PacketBuffer buffer, Object value);

    String getLocalizationString(Object object);

    Optional<T> getValueOf(Object object);

    Set<T> getValues();

}
