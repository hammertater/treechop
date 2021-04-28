package ht.treechop.common.network;

import ht.treechop.client.Client;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerConfirmSettingsPacket implements IMessage {

    private List<ConfirmedSetting> settings;

    public ServerConfirmSettingsPacket() {
    }

    public ServerConfirmSettingsPacket(final List<ConfirmedSetting> settings) {
        this.settings = settings;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(settings.size());
        settings.forEach(setting -> ConfirmedSetting.encode(buffer, setting));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int numSettings = buffer.readInt();
        this.settings = IntStream.range(0, numSettings)
                .mapToObj($ -> ConfirmedSetting.decode(buffer))
                .collect(Collectors.toList());
    }

    public static class Handler implements IMessageHandler<ServerConfirmSettingsPacket, IMessage> {
        @Override
        public IMessage onMessage(ServerConfirmSettingsPacket message, MessageContext context) {
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(
                    () -> message.settings.forEach(ServerConfirmSettingsPacket::processSingleSetting)
            );
            return null;
        }
    }

    private static void processSingleSetting(ConfirmedSetting setting) {
        Client.getChopSettings().accept(setting.getField(), setting.getValue());
        setting.event.run(setting);
    }

}
