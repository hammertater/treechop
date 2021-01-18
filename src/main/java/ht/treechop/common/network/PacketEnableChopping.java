package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketEnableChopping {

    private final boolean choppingEnabled;

    public PacketEnableChopping(boolean choppingEnabled) {
        this.choppingEnabled = choppingEnabled;
    }

    public static void encode(PacketEnableChopping message, PacketBuffer buffer) {
        buffer.writeBoolean(message.choppingEnabled);
    }

    public static PacketEnableChopping decode(PacketBuffer buffer) {
        return new PacketEnableChopping(buffer.readBoolean());
    }

    public static void handle(PacketEnableChopping message, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = Objects.requireNonNull(context.get().getSender());
                if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
                    ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                    chopSettings.setChoppingEnabled(message.choppingEnabled);
                    player.sendMessage(TreeChopMod.makeText("Chopping " + (chopSettings.getChoppingEnabled() ? "ON" : "OFF")), Util.DUMMY_UUID);
                } else {
                    player.sendMessage(TreeChopMod.makeText("Chopping ON" + TextFormatting.RED + " (you are not permitted to disable chopping)"), Util.DUMMY_UUID);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

}
