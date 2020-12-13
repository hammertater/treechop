package ht.treechop.common.network;

import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
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
                ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                chopSettings.setChoppingEnabled(message.choppingEnabled);
                player.sendMessage(new StringTextComponent("[TreeChop] ").mergeStyle(TextFormatting.GRAY).append(new StringTextComponent("Chopping " + (message.choppingEnabled ? "ON" : "OFF")).mergeStyle(TextFormatting.WHITE)), Util.DUMMY_UUID);
            });
            context.get().setPacketHandled(true);
        }
    }

}
