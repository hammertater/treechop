package ht.treechop.common.settings.codec;

import io.netty.buffer.ByteBuf;

import java.util.Optional;
import java.util.Set;

public interface SimpleCodec<T> {

    T decode(ByteBuf buffer);

    void encode(ByteBuf buffer, Object value);

    String getLocalizationString(Object object);

    Optional<T> getValueOf(Object object);

    Set<T> getValues();
}
