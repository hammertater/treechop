package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerConfirmSettingsPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = TreeChop.resource("server_confirm_settings");
    public static final CustomPacketPayload.Type<ServerConfirmSettingsPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ServerConfirmSettingsPacket> STREAM_CODEC = CustomPacketPayload.codec(
            ServerConfirmSettingsPacket::encode, ServerConfirmSettingsPacket::decode
    );

    private final List<ConfirmedSetting> settings;

    public ServerConfirmSettingsPacket(final List<ConfirmedSetting> settings) {
        this.settings = settings;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(settings.size());
        settings.forEach(setting -> setting.encode(buffer));
    }

    public static ServerConfirmSettingsPacket decode(FriendlyByteBuf buffer) {
        int numSettings = buffer.readInt();
        List<ConfirmedSetting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> ConfirmedSetting.decode(buffer))
                .collect(Collectors.toList());

        return new ServerConfirmSettingsPacket(settings);
    }

    public void handle() {
        settings.forEach(ServerConfirmSettingsPacket::processSingleSetting);
    }

    private static void processSingleSetting(ConfirmedSetting setting) {
        Client.getChopSettings().accept(setting.getField(), setting.getValue());
        setting.event.run(setting);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
