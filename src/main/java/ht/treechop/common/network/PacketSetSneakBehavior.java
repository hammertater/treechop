package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.SneakBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.EnumUtils;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSetSneakBehavior {

    private final SneakBehavior sneakBehavior;

    public PacketSetSneakBehavior(SneakBehavior sneakBehavior) {
        this.sneakBehavior = sneakBehavior;
    }

    public static void encode(PacketSetSneakBehavior message, PacketBuffer buffer) {
        buffer.writeString(message.sneakBehavior.name());
    }

    public static PacketSetSneakBehavior decode(PacketBuffer buffer) {
        final SneakBehavior defaultBehavior = new ChopSettings().getSneakBehavior();
        SneakBehavior sneakBehavior = EnumUtils.getEnum(SneakBehavior.class, buffer.readString(SneakBehavior.maxNameLength));
        return new PacketSetSneakBehavior((sneakBehavior != null) ? sneakBehavior : defaultBehavior);
    }

    @SuppressWarnings("ConstantConditions")
    public static void handle(PacketSetSneakBehavior message, Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
                    ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                    chopSettings.setSneakBehavior(message.sneakBehavior);
                    player.sendMessage(TreeChopMod.makeText("Sneak behavior " + chopSettings.getSneakBehavior().getString()), UUID.randomUUID());
                } else {
                    player.sendMessage(TreeChopMod.makeText("Sneak behavior " + SneakBehavior.NONE.getString() + TextFormatting.RED + " (you are not permitted to disable chopping or felling)"), UUID.randomUUID());
                }
            });
            context.get().setPacketHandled(true);
        }
    }

}
