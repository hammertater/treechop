package ht.treechop.common.settings.codec;

import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanCodec extends AbstractSimpleCodec<Boolean> {

    public static final Set<Boolean> values = Collections.unmodifiableSet(Stream.of(
            Boolean.TRUE, Boolean.FALSE
    ).collect(Collectors.toSet()));

    @Override
    public Set<Boolean> getValues() {
        return values;
    }

    @Override
    public Boolean decode(ByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer, Object value) {
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
