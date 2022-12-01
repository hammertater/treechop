package ht.treechop.common.network;

import ht.treechop.TreeChop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ForgePacketHandler {
    private static final String PROTOCOL = "7";
    protected static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TreeChop.MOD_ID + "-channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
}
