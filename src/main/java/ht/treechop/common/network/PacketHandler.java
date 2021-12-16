package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

// See https://github.com/Vazkii/Botania/blob/7e1d89a1d6deda7286744e3b7c55369b2cf5e533/src/main/java/vazkii/botania/common/network/PacketHandler.java
public final class PacketHandler {
    private static final String PROTOCOL = "7";
    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TreeChopMod.MOD_ID + "-channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SuppressWarnings("UnusedAssignment")
    public static void init() {
        int id = 0;
        HANDLER.registerMessage(id++, ClientRequestSettingsPacket.class, ClientRequestSettingsPacket::encode, ClientRequestSettingsPacket::decode, ClientRequestSettingsPacket::handle);
        HANDLER.registerMessage(id++, ServerConfirmSettingsPacket.class, ServerConfirmSettingsPacket::encode, ServerConfirmSettingsPacket::decode, ServerConfirmSettingsPacket::handle);
        HANDLER.registerMessage(id++, ServerPermissionsPacket.class, ServerPermissionsPacket::encode, ServerPermissionsPacket::decode, ServerPermissionsPacket::handle);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    public static void sendTo(ServerPlayer playerMP, Object toSend) {
        HANDLER.sendTo(toSend, playerMP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    private PacketHandler() {}
}
