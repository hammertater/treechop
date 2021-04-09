package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

public class EnumCodec<T extends Enum<T>> extends AbstractSimpleCodec<T> {

    private final Class<T> enumType;

    public EnumCodec(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T decode(PacketBuffer buffer) {
        try {
            return Enum.valueOf(enumType, buffer.readString());
        } catch (IllegalArgumentException e) {
            return enumType.getEnumConstants()[0];
        }
    }

    @Override
    public void encode(PacketBuffer buffer, Object object) {
        // Use the name instead of the ordinal just to be extra safe
        buffer.writeString(getValueOf(object).map(Enum::name).orElse(""));
    }

    @Override
    public Class<T> getTypeClass() {
        return enumType;
    }

}
