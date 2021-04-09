package ht.treechop.common.settings;

import ht.treechop.common.network.SingleSetting;
import ht.treechop.common.settings.codec.Codecs;
import ht.treechop.common.settings.codec.SimpleCodec;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

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

    public String getFancyName() {
        return I18n.format(langKey);
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void encode(PacketBuffer buffer, Object value) {
        buffer.writeByte(ordinal());
        codec.encode(buffer, value);
    }

    private Object decodeValue(PacketBuffer buffer) {
        return codec.decode(buffer);
    }

    public static SingleSetting decode(PacketBuffer buffer) {
        Setting field = Setting.values()[buffer.readByte()];
        Object value = field.decodeValue(buffer);
        return new SingleSetting(field, value);
    }

    public String getValueName(Object value) {
        return I18n.format(codec.getLocalizationString(value));
    }
}
