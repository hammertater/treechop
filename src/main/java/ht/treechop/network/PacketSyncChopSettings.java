package ht.treechop.network;

import ht.treechop.TreeChopMod;
import ht.treechop.capabilities.ChopSettings;
import ht.treechop.capabilities.ChopSettingsCapability;
import ht.treechop.client.Client;
import ht.treechop.config.SneakBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncChopSettings {

    private ChopSettings chopSettings;

    public PacketSyncChopSettings(ChopSettings chopSettings) {
        this.chopSettings = chopSettings;
    }

    public static void encode(PacketSyncChopSettings message, PacketBuffer buffer) {
        ChopSettings chopSettings = message.chopSettings;
        buffer.writeBoolean(chopSettings.getChoppingEnabled());
        buffer.writeBoolean(chopSettings.getFellingEnabled());
        buffer.writeString(chopSettings.getSneakBehavior().name());
    }

    public static PacketSyncChopSettings decode(PacketBuffer buffer) {
        ChopSettings chopSettings = new ChopSettings();
        chopSettings.setChoppingEnabled(buffer.readBoolean());
        chopSettings.setFellingEnabled(buffer.readBoolean());
        chopSettings.setSneakBehavior(SneakBehavior.valueOf(buffer.readString()));
        return new PacketSyncChopSettings(chopSettings);
    }

    public static void handle(PacketSyncChopSettings message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isServer()) {
                handleOnServer(message, context);
            } else {
                handleOnClient(message);
            }
        });
        context.get().setPacketHandled(true);
    }

    private static void handleOnClient(PacketSyncChopSettings message) {
        TreeChopMod.LOGGER.info("Received chop settings from server");
        ChopSettings chopSettings = Client.getChopSettings();
        chopSettings.copyFrom(message.chopSettings);
    }

    public static void handleOnServer(PacketSyncChopSettings message, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        ChopSettingsCapability chopSettings = player.getCapability(ChopSettingsCapability.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("Missing chop settings for player " + player.getScoreboardName()));

        if (!chopSettings.isSynced()) {
            TreeChopMod.LOGGER.info("Received chop settings from player " + player.getScoreboardName());
            chopSettings.copyFrom(message.chopSettings);
            chopSettings.setSynced();
        }

        TreeChopMod.LOGGER.info("Sending chop settings to player " + player.getScoreboardName());
        PacketHandler.sendTo(player, new PacketSyncChopSettings(chopSettings));
    }

}
