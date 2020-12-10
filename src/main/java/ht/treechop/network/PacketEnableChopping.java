package ht.treechop.network;

import ht.treechop.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketEnableChopping {

    private boolean choppingEnabled;

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
                ServerPlayerEntity player = context.get().getSender();
                ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                chopSettings.setChoppingEnabled(message.choppingEnabled);
                player.sendMessage(new StringTextComponent("[TreeChop] ").applyTextStyle(TextFormatting.GRAY).appendSibling(new StringTextComponent("Chopping " + (message.choppingEnabled ? "ON" : "OFF")).applyTextStyle(TextFormatting.WHITE)));
            });
            context.get().setPacketHandled(true);
        }
    }

}
