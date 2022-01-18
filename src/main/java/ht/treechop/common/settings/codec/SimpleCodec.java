package ht.treechop.common.settings.codec;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.Set;

public interface SimpleCodec<T> {

    T decode(FriendlyByteBuf buffer);

    void encode(FriendlyByteBuf buffer, Object value);

    String getLocalizationString(Object object);

    Optional<T> getValueOf(Object object);

    Set<T> getValues();
}
