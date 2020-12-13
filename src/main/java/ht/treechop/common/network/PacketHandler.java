package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        HANDLER.registerMessage(id++, PacketEnableChopping.class, PacketEnableChopping::encode, PacketEnableChopping::decode, PacketEnableChopping::handle);
        HANDLER.registerMessage(id++, PacketEnableFelling.class, PacketEnableFelling::encode, PacketEnableFelling::decode, PacketEnableFelling::handle);
        HANDLER.registerMessage(id++, PacketSetSneakBehavior.class, PacketSetSneakBehavior::encode, PacketSetSneakBehavior::decode, PacketSetSneakBehavior::handle);
        HANDLER.registerMessage(id++, PacketSyncChopSettings.class, PacketSyncChopSettings::encode, PacketSyncChopSettings::decode, PacketSyncChopSettings::handle);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    public static void sendTo(ServerPlayerEntity playerMP, Object toSend) {
        HANDLER.sendTo(toSend, playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    private PacketHandler() {}
}
