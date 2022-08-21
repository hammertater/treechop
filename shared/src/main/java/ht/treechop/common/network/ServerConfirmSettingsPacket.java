package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerConfirmSettingsPacket implements CustomPacket {
    private static final ResourceLocation id = TreeChop.resource("server_confirm_settings");
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

    public static void handle(ServerConfirmSettingsPacket message, ServerPlayer sender) {
        message.settings.forEach(ServerConfirmSettingsPacket::processSingleSetting);
    }

    private static void processSingleSetting(ConfirmedSetting setting) {
        Client.getChopSettings().accept(setting.getField(), setting.getValue());
        setting.event.run(setting);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
}
