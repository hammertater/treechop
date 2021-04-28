package ht.treechop.common.settings.codec;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

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
    public T decode(ByteBuf buffer) {
        try {
            int numBytes = buffer.readInt();
            String string = buffer.readCharSequence(numBytes, Charsets.UTF_8).toString();
            return Enum.valueOf(enumType, string);
        } catch (IllegalArgumentException e) {
            return enumType.getEnumConstants()[0];
        }
    }

    @Override
    public void encode(ByteBuf buffer, Object object) {
        // Use the name instead of the ordinal just to be extra safe
        byte[] stringBytes = getValueOf(object).map(Enum::name).orElse("").getBytes(Charsets.UTF_8);
        buffer.writeInt(stringBytes.length);
        buffer.writeBytes(stringBytes);
    }

    @Override
    public Class<T> getTypeClass() {
        return enumType;
    }

}
