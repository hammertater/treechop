package ht.treechop.common.network;

import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.server.Server;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientRequestSettingsPacket implements IMessage {

    private List<Setting> settings;
    private Event event;

    public ClientRequestSettingsPacket() {
    }

    public ClientRequestSettingsPacket(final List<Setting> settings, Event event) {
        this.settings = settings;
        this.event = event;
    }

    public ClientRequestSettingsPacket(SettingsField field, Object value) {
        this(Collections.singletonList(new Setting(field, value)), Event.REQUEST);
    }

    public ClientRequestSettingsPacket(ClientChopSettings chopSettings) {
        this(chopSettings.getAll(), Event.FIRST_TIME_SYNC);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        event.encode(buffer);
        buffer.writeInt(settings.size());
        settings.forEach(setting -> Setting.encode(buffer, setting));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.event = Event.decode(buffer);
        int numSettings = buffer.readInt();
        this.settings = IntStream.range(0, numSettings)
                .mapToObj($ -> Setting.decode(buffer))
                .collect(Collectors.toList());
    }

    public static class Handler implements IMessageHandler<ClientRequestSettingsPacket, IMessage> {
        @Override
        public IMessage onMessage(ClientRequestSettingsPacket message, MessageContext context) {
            if (context.side == Side.SERVER) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServer().addScheduledTask(
                        () -> processSettingsRequest(message, player)
                );
            }
            return null;
        }
    }

    private static <T> void processSettingsRequest(ClientRequestSettingsPacket message, EntityPlayerMP player) {
        ChopSettingsCapability.forPlayer(player).ifPresent(capability -> processSettingsRequest(capability, message, player));
    }

    private static void processSettingsRequest(ChopSettingsCapability capability, ClientRequestSettingsPacket message, EntityPlayerMP player) {
        List<Setting> settings = (message.event == Event.FIRST_TIME_SYNC && capability.isSynced())
                ? capability.getAll()
                : message.settings;

        List<ConfirmedSetting> confirmedSettings = settings.stream()
                .map(setting -> processSingleSettingRequest(setting, player, capability, message.event))
                .collect(Collectors.toList());

        PacketHandler.sendTo(player, new ServerConfirmSettingsPacket(confirmedSettings));

        if (message.event == Event.FIRST_TIME_SYNC) {
            if (!capability.isSynced()) {
                capability.setSynced();
            }

            PacketHandler.sendTo(player, new ServerPermissionsPacket(Server.getPermissions()));
        }
    }

    private static ConfirmedSetting processSingleSettingRequest(Setting setting, EntityPlayerMP player, ChopSettings chopSettings, Event requestEvent) {
        ConfirmedSetting.Event confirmEvent;
        if (playerHasPermission(player, setting)) {
            chopSettings.set(setting);
            confirmEvent = ConfirmedSetting.Event.ACCEPT;
        } else {
            Setting defaultSetting = getDefaultSetting(player, setting);
            chopSettings.set(defaultSetting);
            confirmEvent = ConfirmedSetting.Event.DENY;
        }

        if (requestEvent == Event.FIRST_TIME_SYNC) {
            confirmEvent = ConfirmedSetting.Event.SILENT;
        }

        SettingsField field = setting.getField();
        return new ConfirmedSetting(new Setting(field, chopSettings.get(field)), confirmEvent);
    }

    private static Setting getDefaultSetting(EntityPlayerMP player, Setting setting) {
        return Server.getDefaultPlayerSettings().getSetting(setting.getField());
    }

    private static boolean playerHasPermission(EntityPlayer player, Setting setting) {
        return Server.getPermissions().isPermitted(setting);
    }

    private enum Event {
        FIRST_TIME_SYNC,
        REQUEST
        ;

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
