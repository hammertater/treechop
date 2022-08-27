package ht.treechop.client;

import ht.treechop.client.model.ChoppedLogModelProvider;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

@Environment(EnvType.CLIENT)
public class FabricClient extends Client implements ClientModInitializer {
    static {
        Client.instance = new FabricClient();
    }

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new ChoppedLogModelProvider());
        registerPackets();
    }

    private void registerPackets() {
        // Note: buffers get freed before deferred code can run, so we must process them immediately
        ClientPlayNetworking.registerGlobalReceiver(ServerConfirmSettingsPacket.ID, (client, handler, buffer, sender) -> {
            ServerConfirmSettingsPacket packet = ServerConfirmSettingsPacket.decode(buffer);
            client.execute(() -> ServerConfirmSettingsPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerPermissionsPacket.ID, (client, handler, buffer, sender) -> {
            ServerPermissionsPacket packet = ServerPermissionsPacket.decode(buffer);
            client.execute(() -> ServerPermissionsPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerUpdateChopsPacket.ID, (client, handler, buffer, sender) -> {
            ServerUpdateChopsPacket packet = ServerUpdateChopsPacket.decode(buffer);
            client.execute(() -> {
                ServerUpdateChopsPacket.handle(packet);
            });
        });
    }

    @Override
    void sendToServer(CustomPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(packet.getId(), packet.encode(buffer));
    }
}
