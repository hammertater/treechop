package ht.treechop.common.network;

import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Setting;
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

    private final List<SingleSetting> settings;
    private final Event event;

    public ClientRequestSettingsPacket(final List<SingleSetting> settings, Event event) {
        this.settings = settings;
        this.event = event;
    }

    public ClientRequestSettingsPacket(Setting field, Object value) {
        this(Collections.singletonList(new SingleSetting(field, value)), Event.REQUEST);
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
        List<SingleSetting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> SingleSetting.decode(buffer))
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
        List<ConfirmedSetting> settings;
        if (message.event == Event.FIRST_TIME_SYNC && capability.isSynced()) {
            settings = capability.getAll().stream().map(setting -> new ConfirmedSetting(setting, ConfirmedSetting.Event.OVERRIDE))
                    .map(setting -> processSingleSettingRequest(setting, player, capability, message.event))
                    .collect(Collectors.toList());
        } else {
            settings = message.settings.stream()
                    .map(setting -> processSingleSettingRequest(setting, player, capability, message.event))
                    .collect(Collectors.toList());
        }

        if (message.event == Event.FIRST_TIME_SYNC && !capability.isSynced()) {
            capability.setSynced();
        }

        PacketHandler.sendTo(player, new ServerConfirmSettingsPacket(settings));
    }

    private static ConfirmedSetting processSingleSettingRequest(SingleSetting setting, ServerPlayerEntity player, ChopSettings chopSettings, Event requestEvent) {
        ConfirmedSetting.Event confirmEvent;
        if (playerHasPermission(player, setting)) {
            chopSettings.set(setting);
            confirmEvent = ConfirmedSetting.Event.ACCEPT;
        } else {
            confirmEvent = ConfirmedSetting.Event.DENY;
        }

        if (requestEvent == Event.FIRST_TIME_SYNC) {
            confirmEvent = ConfirmedSetting.Event.OVERRIDE;
        }

        Setting field = setting.getField();
        return new ConfirmedSetting(new SingleSetting(field, chopSettings.get(field)), confirmEvent);
    }

    private static boolean playerHasPermission(PlayerEntity player, SingleSetting setting) {
        return true; // TODO
//        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
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
