package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
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
                if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
                    ChopSettingsCapability.forPlayer(player).ifPresent(
                            chopSettings -> {
                                chopSettings.setFellingEnabled(message.fellingEnabled);
                                player.sendMessage(TreeChopMod.makeText("Felling " + (message.fellingEnabled ? "ON" : "OFF")), Util.DUMMY_UUID);
                            }
                    );
                } else {
                    player.sendMessage(TreeChopMod.makeText("Felling ON" + TextFormatting.RED + " (you are not permitted to disable felling)"), Util.DUMMY_UUID);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

}
