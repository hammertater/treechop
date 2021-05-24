package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class ConfirmedSetting extends Setting {

    protected final Event event;

    public ConfirmedSetting(Setting setting, Event event) {
        super(setting.getField(), setting.getValue());
        this.event = event;
    }

    public static void encode(ByteBuf buffer, ConfirmedSetting setting) {
        Setting.encode(buffer, setting);
        setting.event.encode(buffer);
    }

    public static ConfirmedSetting decode(ByteBuf buffer) {
        Setting setting = Setting.decode(buffer);
        Event event = Event.decode(buffer);
        return new ConfirmedSetting(setting, event);
    }

    public enum Event {
        ACCEPT {
            @Override
            public void run(ConfirmedSetting setting) {
                if (Minecraft.getMinecraft().currentScreen == null) {
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
                if (Minecraft.getMinecraft().currentScreen == null) {
                    String fieldName = setting.getField().getFancyName();
                    String valueName = setting.getField().getValueName(setting.getValue());
                    if (ConfigHandler.CLIENT.showFeedbackMessages.get()) {
                        TreeChopMod.showText(String.format(
                                "%s %s %s(%s)",
                                fieldName,
                                valueName,
                                TextFormatting.RED,
                                I18n.format("treechop.setting.missing_permissions")
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

        public static Event decode(ByteBuf buffer) {
            int ordinal = buffer.readByte() % values.length;
            return Event.values[ordinal];
        }

        public void encode(ByteBuf buffer) {
            buffer.writeByte(ordinal());
        }
    }
}
