package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

// See https://github.com/Vazkii/Botania/blob/7e1d89a1d6deda7286744e3b7c55369b2cf5e533/src/main/java/vazkii/botania/common/network/PacketHandler.java
public final class PacketHandler {
    public static final SimpleNetworkWrapper HANDLER = new SimpleNetworkWrapper(TreeChopMod.MOD_ID + "-channel");

    @SuppressWarnings("UnusedAssignment")
    public static void init() {
        int id = 0;
        HANDLER.registerMessage(ClientRequestSettingsPacket.Handler.class, ClientRequestSettingsPacket.class, id++, Side.SERVER);
        HANDLER.registerMessage(ServerConfirmSettingsPacket.Handler.class, ServerConfirmSettingsPacket.class, id++, Side.CLIENT);
        HANDLER.registerMessage(ServerPermissionsPacket.Handler.class, ServerPermissionsPacket.class, id++, Side.CLIENT);
    }

    public static void sendToServer(IMessage message) {
        HANDLER.sendToServer(message);
    }

    public static void sendTo(EntityPlayerMP player, IMessage message) {
        HANDLER.sendTo(message, player);
    }
}
