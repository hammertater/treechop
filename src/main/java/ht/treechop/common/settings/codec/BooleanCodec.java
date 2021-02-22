package ht.treechop.common.settings.codec;

import net.minecraft.network.PacketBuffer;

public class BooleanCodec implements SimpleCodec<Boolean> {

    @Override
    public Boolean decode(PacketBuffer buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void encode(PacketBuffer buffer, Object value) {
        buffer.writeBoolean((Boolean)value);
    }

}
