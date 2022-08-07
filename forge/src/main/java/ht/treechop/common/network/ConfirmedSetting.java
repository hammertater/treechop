package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ForgeConfigHandler;
import ht.treechop.common.settings.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;

public class ConfirmedSetting extends Setting {

    protected final Event event;

    public ConfirmedSetting(Setting setting, Event event) {
        super(setting.getField(), setting.getValue());
        this.event = event;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        event.encode(buffer);
    }

    public static ConfirmedSetting decode(FriendlyByteBuf buffer) {
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
                    if (ForgeConfigHandler.CLIENT.showFeedbackMessages.get()) {
                        TreeChop.showText(String.format(
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
                    if (ForgeConfigHandler.CLIENT.showFeedbackMessages.get()) {
                        TreeChop.showText(String.format(
                                "%s %s %s(%s)",
                                fieldName,
                                valueName,
                                ChatFormatting.RED,
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

        public static Event decode(FriendlyByteBuf buffer) {
            int ordinal = buffer.readByte() % values.length;
            return Event.values[ordinal];
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeByte(ordinal());
        }
    }
}
