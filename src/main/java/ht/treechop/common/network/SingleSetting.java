package ht.treechop.common.network;

import ht.treechop.common.settings.Setting;
import net.minecraft.network.PacketBuffer;

public class SingleSetting {

    protected Setting field;
    protected Object value;

    public SingleSetting(Setting field, Object value) {
        this.field = field;
        this.value = value;
    }

    public void encode(PacketBuffer buffer) {
        field.encode(buffer, value);
    }

    public static SingleSetting decode(PacketBuffer buffer) {
        return Setting.decode(buffer);
    }

    public Setting getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

}
