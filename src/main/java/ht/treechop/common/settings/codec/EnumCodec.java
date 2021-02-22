package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

public class EnumCodec<T extends Enum<T>> implements SimpleCodec<T> {

    private final Class<T> enumType;

    public EnumCodec(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T decode(PacketBuffer buffer) {
        return Enum.valueOf(enumType, buffer.readString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(PacketBuffer buffer, Object value) {
        if (enumType.isInstance(value)) {
            buffer.writeString(((T)value).name());
        }
    }

}
