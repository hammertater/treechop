package ht.treechop.common.network;

import ht.treechop.client.Client;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerConfirmSettingsPacket {

    private final List<ConfirmedSetting> settings;

    public ServerConfirmSettingsPacket(final List<ConfirmedSetting> settings) {
        this.settings = settings;
    }

    public static void encode(ServerConfirmSettingsPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.settings.size());
        message.settings.forEach(setting -> setting.encode(buffer));
    }

    public static ServerConfirmSettingsPacket decode(FriendlyByteBuf buffer) {
        int numSettings = buffer.readInt();
        List<ConfirmedSetting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> ConfirmedSetting.decode(buffer))
                .collect(Collectors.toList());

        return new ServerConfirmSettingsPacket(settings);
    }

    public static void handle(ServerConfirmSettingsPacket message, Supplier<NetworkEvent.Context> context) {
        if (!context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(() -> message.settings.forEach(ServerConfirmSettingsPacket::processSingleSetting));
            context.get().setPacketHandled(true);
        }
    }

    private static void processSingleSetting(ConfirmedSetting setting) {
        Client.getChopSettings().accept(setting.getField(), setting.getValue());
        setting.event.run(setting);
    }

}
