package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

public class ConfirmedSetting extends Setting {

    protected final Event event;

    public ConfirmedSetting(Setting setting, Event event) {
        super(setting.getField(), setting.getValue());
        this.event = event;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        event.encode(buffer);
    }

    public static ConfirmedSetting decode(PacketBuffer buffer) {
        Setting setting = Setting.decode(buffer);
        Event event = Event.decode(buffer);
        return new ConfirmedSetting(setting, event);
    }

    public enum Event {
        ACCEPT {
            @Override
            public void run(ConfirmedSetting setting) {
                if (Minecraft.getInstance().screen == null) {
                    String fieldName = setting.getField().getFancyName();
                    String valueName = setting.getField().getValueName(setting.getValue());
                    if (ConfigHandler.CLIENT.showFeedbackMessages.get()) {
                        TreeChopMod.showText(String.format(
                                "%s %s",
                                fieldName,
                                valueName
                        ));
                    }
                }
            }
        },
        DENY {
            @Override
            public void run(ConfirmedSetting setting) {
                if (Minecraft.getInstance().screen == null) {
                    String fieldName = setting.getField().getFancyName();
                    String valueName = setting.getField().getValueName(setting.getValue());
                    if (ConfigHandler.CLIENT.showFeedbackMessages.get()) {
                        TreeChopMod.showText(String.format(
                                "%s %s %s(%s)",
                                fieldName,
                                valueName,
                                TextFormatting.RED,
                                I18n.get("treechop.setting.missing_permissions")
                        ));
                    }
                }
            }
        },
        SILENT
        ;

        public void run(ConfirmedSetting message) {
        }

        private static final Event[] values = Event.values();

        public static Event decode(PacketBuffer buffer) {
            int ordinal = buffer.readByte() % values.length;
            return Event.values[ordinal];
        }

        public void encode(PacketBuffer buffer) {
            buffer.writeByte(ordinal());
        }
    }
}
