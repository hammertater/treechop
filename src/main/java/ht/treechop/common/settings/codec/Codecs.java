package ht.treechop.common.settings.codec;

import ht.treechop.common.settings.SneakBehavior;

import java.util.HashMap;
import java.util.Map;

public class Codecs {

    private static Map<Class<?>, SimpleCodec<?>> codecs = new HashMap<>();

    static {
        codecs.put(Boolean.class, new BooleanCodec());
        codecs.put(SneakBehavior.class, new EnumCodec<>(SneakBehavior.class));
    }

    public static SimpleCodec<?> forType(Class<?> type) {
        return codecs.get(type);
    }

}
