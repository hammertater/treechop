package ht.treechop.network;

import ht.treechop.TreeChopMod;
import ht.treechop.capabilities.ChopSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.network.NetworkEvent;

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
                ChopSettings chopSettings = player.getCapability(ChopSettings.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("Player missing chop settings for " + player.getName()));
                chopSettings.setChoppingEnabled(message.choppingEnabled);
                player.sendMessage(new StringTextComponent("[TreeChop] ").mergeStyle(TextFormatting.GRAY).append(new StringTextComponent("Chopping " + (message.choppingEnabled ? "ON" : "OFF")).mergeStyle(TextFormatting.WHITE)), Util.DUMMY_UUID);
            });
        }
    }

}
