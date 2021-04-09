package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

public class BooleanCodec extends AbstractSimpleCodec<Boolean> {

    @Override
    public Boolean decode(PacketBuffer buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void encode(PacketBuffer buffer, Object value) {
        buffer.writeBoolean((Boolean)value);
    }

    @Override
    public String localizeSafe(Boolean object) {
        return object ? "treechop.gui.big_on" : "treechop.gui.big_off";
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

}
