package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerPermissionsPacket implements CustomPacket {
    public static final ResourceLocation ID = TreeChop.resource("server_permissions");
    private final Permissions permissions;

    public ServerPermissionsPacket(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public FriendlyByteBuf encode(FriendlyByteBuf buffer) {
        Set<Setting> settings = permissions.getPermittedSettings();
        buffer.writeInt(settings.size());
        settings.forEach(setting -> setting.encode(buffer));
        return buffer;
    }

    public static ServerPermissionsPacket decode(FriendlyByteBuf buffer) {
        int numSettings = buffer.readInt();
        List<Setting> settings = IntStream.range(0, numSettings)
                .mapToObj($ -> Setting.decode(buffer))
                .collect(Collectors.toList());

        return new ServerPermissionsPacket(new Permissions(settings));
    }

    public static void handle(ServerPermissionsPacket message) {
        Client.updatePermissions(message.permissions);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
