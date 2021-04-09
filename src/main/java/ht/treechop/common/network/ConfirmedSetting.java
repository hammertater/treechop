package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

public class ConfirmedSetting extends SingleSetting {

    protected final Event event;

    public ConfirmedSetting(SingleSetting setting, Event event) {
        super(setting.getField(), setting.getValue());
        this.event = event;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        event.encode(buffer);
    }

    public static ConfirmedSetting decode(PacketBuffer buffer) {
        SingleSetting setting = SingleSetting.decode(buffer);
        Event event = Event.decode(buffer);
        return new ConfirmedSetting(setting, event);
    }

    public enum Event {
        ACCEPT {
            @Override
            public void run(ConfirmedSetting setting) {
                if (Minecraft.getInstance().currentScreen == null) {
                    String fieldName = setting.field.getFancyName();
                    String valueName = setting.field.getValueName(setting.getValue());
                    TreeChopMod.showText(String.format(
                            "%s %s",
                            fieldName,
                            valueName
                    ));
                }
            }
        },
        DENY {
            @Override
            public void run(ConfirmedSetting setting) {
                if (Minecraft.getInstance().currentScreen == null) {
                    String fieldName = setting.field.getFancyName();
                    String valueName = setting.field.getValueName(setting.getValue());
                    TreeChopMod.showText(String.format(
                            "%s %s %s(%s)",
                            fieldName,
                            valueName,
                            TextFormatting.RED,
                            I18n.format("treechop.setting.missing_permissions")
                    ));
                }
            }
        },
        OVERRIDE
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
