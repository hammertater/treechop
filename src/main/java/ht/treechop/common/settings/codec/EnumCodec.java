package ht.treechop.common.settings.codec;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumCodec<T extends Enum<T>> extends AbstractSimpleCodec<T> {

    private final Class<T> enumType;
    public final Set<T> values;

    public EnumCodec(Class<T> enumType) {
        this.enumType = enumType;
        this.values = Collections.unmodifiableSet(Arrays.stream(enumType.getEnumConstants()).collect(Collectors.toSet()));
    }

    @Override
    public Set<T> getValues() {
        return values;
    }

    @Override
    public T decode(FriendlyByteBuf buffer) {
        try {
            return Enum.valueOf(enumType, buffer.readUtf(64));
        } catch (IllegalArgumentException e) {
            return enumType.getEnumConstants()[0];
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer, Object object) {
        // Use the name instead of the ordinal just to be extra safe
        buffer.writeUtf(getValueOf(object).map(Enum::name).orElse(""));
    }

    @Override
    public Class<T> getTypeClass() {
        return enumType;
    }

}
