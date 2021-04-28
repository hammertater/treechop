package ht.treechop.common.network;

import ht.treechop.client.Client;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerPermissionsPacket implements IMessage {

    private Permissions permissions;

    public ServerPermissionsPacket() {
    }

    public ServerPermissionsPacket(Permissions permissions) {
        this.permissions = permissions;
    }

    public void toBytes(ByteBuf buffer) {
        Set<Setting> settings = permissions.getPermittedSettings();
        buffer.writeInt(settings.size());
        settings.forEach(setting -> Setting.encode(buffer, setting));
    }

    public void fromBytes(ByteBuf buffer) {
        int numSettings = buffer.readInt();
        List<Setting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> Setting.decode(buffer))
                .collect(Collectors.toList());

        this.permissions = new Permissions(settings);
    }

    public static class Handler implements IMessageHandler<ServerPermissionsPacket, IMessage> {
        @Override
        public IMessage onMessage(ServerPermissionsPacket message, MessageContext context) {
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(
                    () -> Client.updatePermissions(message.permissions)
            );
            return null;
        }
    }

}
