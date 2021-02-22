package ht.treechop.common.settings;

import ht.treechop.common.settings.codec.Codecs;
import ht.treechop.common.settings.codec.SimpleCodec;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.tuple.Pair;

public enum Setting {
    CHOPPING("treechop.setting.chopping", Boolean.TRUE),
    FELLING("treechop.setting.felling", Boolean.TRUE),
    SNEAK_BEHAVIOR("treechop.setting.sneak_behavior", SneakBehavior.INVERT_CHOPPING),
    TREES_MUST_HAVE_LEAVES("treechop.setting.trees_must_have_leaves", Boolean.TRUE),
    CHOP_IN_CREATIVE_MODE("treechop.setting.chop_in_creative_mode", Boolean.FALSE)
    ;

    private final String langKey;
    private final SimpleCodec<?> codec;
    private Object defaultValue;

    Setting(String langKey, Object defaultValue) {
        this.langKey = langKey;
        this.defaultValue = defaultValue;
        this.codec = Codecs.forType(defaultValue.getClass());
    }

    public String getLangKey() {
        return langKey;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void encode(PacketBuffer buffer, Object value) {
        buffer.writeInt(ordinal());
        codec.encode(buffer, value);
    }

    private Object decodeValue(PacketBuffer buffer) {
        return codec.decode(buffer);
    }

    public static Pair<Setting, Object> decode(PacketBuffer buffer) {
        Setting setting = Setting.values()[buffer.readInt()];
        Object value = setting.decodeValue(buffer);
        return Pair.of(setting, value);
    }

}
