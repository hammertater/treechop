package ht.treechop.common.network;

import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.server.Server;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientRequestSettingsPacket {

    private final List<Setting> settings;
    private final Event event;

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

    public static void encode(ClientRequestSettingsPacket message, PacketBuffer buffer) {
        message.event.encode(buffer);
        buffer.writeInt(message.settings.size());
        message.settings.forEach(setting -> setting.encode(buffer));
    }

    public static ClientRequestSettingsPacket decode(PacketBuffer buffer) {
        Event event = Event.decode(buffer);
        int numSettings = buffer.readInt();
        List<Setting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> Setting.decode(buffer))
                .collect(Collectors.toList());

        return new ClientRequestSettingsPacket(settings, event);
    }

    public static void handle(ClientRequestSettingsPacket message, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(
                    () -> processSettingsRequest(message, Objects.requireNonNull(context.get().getSender()))
            );
            context.get().setPacketHandled(true);
        }
    }

    private static <T> void processSettingsRequest(ClientRequestSettingsPacket message, ServerPlayerEntity player) {
        ChopSettingsCapability.forPlayer(player).ifPresent(capability -> processSettingsRequest(capability, message, player));
    }

    private static void processSettingsRequest(ChopSettingsCapability capability, ClientRequestSettingsPacket message, ServerPlayerEntity player) {
        List<Setting> settings = (message.event == Event.FIRST_TIME_SYNC && capability.isSynced())
                ? capability.getAll()
                : message.settings;

        List<ConfirmedSetting> confirmedSettings = settings.stream()
                .map(setting -> processSingleSettingRequest(setting, player, capability, message.event))
                .collect(Collectors.toList());;

        PacketHandler.sendTo(player, new ServerConfirmSettingsPacket(confirmedSettings));

        if (message.event == Event.FIRST_TIME_SYNC) {
            if (!capability.isSynced()) {
                capability.setSynced();
            }

            PacketHandler.sendTo(player, new ServerPermissionsPacket(Server.getPermissions()));
        }
    }

    private static ConfirmedSetting processSingleSettingRequest(Setting setting, ServerPlayerEntity player, ChopSettings chopSettings, Event requestEvent) {
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

    private static Setting getDefaultSetting(ServerPlayerEntity player, Setting setting) {
        return Server.getDefaultPlayerSettings().getSetting(setting.getField());
    }

    private static boolean playerHasPermission(PlayerEntity player, Setting setting) {
        return Server.getPermissions().isPermitted(setting);
    }

    private enum Event {
        FIRST_TIME_SYNC,
        REQUEST
        ;

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
