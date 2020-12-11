package ht.treechop.network;

import ht.treechop.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketEnableFelling {

    private final boolean fellingEnabled;

    public PacketEnableFelling(boolean fellingEnabled) {
        this.fellingEnabled = fellingEnabled;
    }

    public static void encode(PacketEnableFelling message, PacketBuffer buffer) {
        buffer.writeBoolean(message.fellingEnabled);
    }

    public static PacketEnableFelling decode(PacketBuffer buffer) {
        return new PacketEnableFelling(buffer.readBoolean());
    }

    @SuppressWarnings("ConstantConditions")
    public static void handle(PacketEnableFelling message, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                ChopSettingsCapability chopSettings = player.getCapability(ChopSettingsCapability.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("Player missing chop settings for " + player.getScoreboardName()));
                chopSettings.setFellingEnabled(message.fellingEnabled);
                player.sendMessage(new StringTextComponent("[TreeChop] ").mergeStyle(TextFormatting.GRAY).append(new StringTextComponent("Felling " + (message.fellingEnabled ? "ON" : "OFF")).mergeStyle(TextFormatting.WHITE)), Util.DUMMY_UUID);
            });
            context.get().setPacketHandled(true);
        }
    }

}
