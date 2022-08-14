package ht.treechop.common.network;

import ht.treechop.client.Client;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerPermissionsPacket {

    private final Permissions permissions;

    public ServerPermissionsPacket(Permissions permissions) {
        this.permissions = permissions;
    }

    public static void encode(ServerPermissionsPacket message, FriendlyByteBuf buffer) {
        Set<Setting> settings = message.permissions.getPermittedSettings();
        buffer.writeInt(settings.size());
        settings.forEach(setting -> setting.encode(buffer));
    }

    public static ServerPermissionsPacket decode(FriendlyByteBuf buffer) {
        int numSettings = buffer.readInt();
        List<Setting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> Setting.decode(buffer))
                .collect(Collectors.toList());

        return new ServerPermissionsPacket(new Permissions(settings));
    }

    public static void handle(ServerPermissionsPacket message, ServerPlayer sender) {
        Client.updatePermissions(message.permissions);
    }

}
