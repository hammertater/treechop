package ht.treechop.network;

import ht.treechop.capabilities.ChopSettingsCapability;
import ht.treechop.config.SneakBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetSneakBehavior {

    private SneakBehavior sneakBehavior;

    public PacketSetSneakBehavior(SneakBehavior sneakBehavior) {
        this.sneakBehavior = sneakBehavior;
    }

    public static void encode(PacketSetSneakBehavior message, PacketBuffer buffer) {
        buffer.writeString(message.sneakBehavior.name());
    }

    public static PacketSetSneakBehavior decode(PacketBuffer buffer) {
        return new PacketSetSneakBehavior(SneakBehavior.valueOf(buffer.readString()));
    }

    public static void handle(PacketSetSneakBehavior message, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                ChopSettingsCapability chopSettings = player.getCapability(ChopSettingsCapability.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("Player missing chop settings for " + player.getScoreboardName()));
                chopSettings.setSneakBehavior(message.sneakBehavior);
                player.sendMessage(new StringTextComponent("[TreeChop] ").mergeStyle(TextFormatting.GRAY).append(new StringTextComponent("Sneak behavior " + message.sneakBehavior.getString()).mergeStyle(TextFormatting.WHITE)), Util.DUMMY_UUID);
            });
        }
    }

}
