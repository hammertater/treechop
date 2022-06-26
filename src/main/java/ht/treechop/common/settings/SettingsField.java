package ht.treechop.common.settings;

import ht.treechop.common.settings.codec.Codecs;
import ht.treechop.common.settings.codec.SimpleCodec;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.Set;
import java.util.stream.Collectors;

public enum SettingsField {
    CHOPPING("choppingEnabled", "treechop.setting.chopping", Boolean.TRUE),
    FELLING("fellingEnabled", "treechop.setting.felling", Boolean.TRUE),
    SNEAK_BEHAVIOR("sneakBehavior", "treechop.setting.sneak_behavior", SneakBehavior.INVERT_CHOPPING),
    TREES_MUST_HAVE_LEAVES("treeMustHaveLeaves", "treechop.setting.trees_must_have_leaves", Boolean.TRUE),
    CHOP_IN_CREATIVE_MODE("chopInCreativeMode", "treechop.setting.chop_in_creative_mode", Boolean.FALSE)
    ;

    public static final SettingsField[] VALUES = values();

    private final String configKey;
    private final String langKey;
    private final SimpleCodec<?> codec;
    private Object defaultValue;

    SettingsField(String configKey, String langKey, Object defaultValue) {
        this.configKey = configKey;
        this.langKey = langKey;
        this.defaultValue = defaultValue;
        this.codec = Codecs.forType(defaultValue.getClass());
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getFancyName() {
        return I18n.get(langKey);
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    // TODO: generify to bytestream
    public void encode(PacketBuffer buffer, Object value) {
        buffer.writeByte(ordinal());
        codec.encode(buffer, value);
    }

    // TODO: generify to bytestream
    private Object decodeValue(PacketBuffer buffer) {
        return codec.decode(buffer);
    }

    // TODO: generify to bytestream
    public static Setting decode(PacketBuffer buffer) {
        SettingsField field = SettingsField.values()[buffer.readByte()];
        Object value = field.decodeValue(buffer);
        return new Setting(field, value);
    }

    public String getValueName(Object value) {
        return I18n.get(codec.getLocalizationString(value));
    }

    public Set<Object> getValues() {
        return codec.getValues().stream().map(a -> (Object)a).collect(Collectors.toSet());
    }
}
