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
        HANDLER.registerMessage(PacketEnableChopping.Handler.class, PacketEnableChopping.class, id++, Side.SERVER);
        HANDLER.registerMessage(PacketEnableFelling.Handler.class, PacketEnableFelling.class, id++, Side.SERVER);
        HANDLER.registerMessage(PacketSetSneakBehavior.Handler.class, PacketSetSneakBehavior.class, id++, Side.SERVER);
        HANDLER.registerMessage(PacketSyncChopSettingsToServer.Handler.class, PacketSyncChopSettingsToServer.class, id++, Side.SERVER);
        HANDLER.registerMessage(PacketSyncChopSettingsToClient.Handler.class, PacketSyncChopSettingsToClient.class, id++, Side.CLIENT);
        HANDLER.registerMessage(PacketRequestChopSettings.Handler.class, PacketRequestChopSettings.class, id++, Side.CLIENT);
    }

    public static void sendToServer(IMessage message) {
        HANDLER.sendToServer(message);
    }

    public static void sendTo(EntityPlayerMP player, IMessage message) {
        HANDLER.sendTo(message, player);
    }
}
