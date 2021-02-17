package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.SneakBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.EnumUtils;

import java.util.function.Supplier;

public class PacketSyncChopSettings {

    private final ChopSettings chopSettings;

    public PacketSyncChopSettings(ChopSettings chopSettings) {
        this.chopSettings = chopSettings;
    }

    public static void encode(PacketSyncChopSettings message, PacketBuffer buffer) {
        ChopSettings chopSettings = message.chopSettings;
        buffer.writeBoolean(chopSettings.getChoppingEnabled());
        buffer.writeBoolean(chopSettings.getFellingEnabled());
        buffer.writeString(chopSettings.getSneakBehavior().name());
        buffer.writeBoolean(chopSettings.getTreesMustHaveLeaves());
        buffer.writeBoolean(chopSettings.getChopInCreativeMode());
    }

    public static PacketSyncChopSettings decode(PacketBuffer buffer) {
        ChopSettings chopSettings = new ChopSettings();
        chopSettings.setChoppingEnabled(buffer.readBoolean());
        chopSettings.setFellingEnabled(buffer.readBoolean());
        SneakBehavior sneakBehavior = EnumUtils.getEnum(SneakBehavior.class, buffer.readString(SneakBehavior.maxNameLength));
        chopSettings.setSneakBehavior((sneakBehavior != null) ? sneakBehavior : chopSettings.getSneakBehavior());
        chopSettings.setTreesMustHaveLeaves(buffer.readBoolean());
        chopSettings.setChopInCreativeMode(buffer.readBoolean());
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

    @SuppressWarnings("ConstantConditions")
    public static void handleOnServer(PacketSyncChopSettings message, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        ChopSettingsCapability.forPlayer(player).ifPresent(
                chopSettings -> {
                    if (!chopSettings.isSynced()) {
                        TreeChopMod.LOGGER.info("Received chop settings from player " + player.getScoreboardName());
                        chopSettings.copyFrom(message.chopSettings);
                        chopSettings.setSynced();
                    }

                    // Force settings through that aren't yet configurable in-game
                    chopSettings.setTreesMustHaveLeaves(message.chopSettings.getTreesMustHaveLeaves());
                    chopSettings.setChopInCreativeMode(message.chopSettings.getChopInCreativeMode());

                    TreeChopMod.LOGGER.info("Sending chop settings to player " + player.getScoreboardName());
                    PacketHandler.sendTo(player, new PacketSyncChopSettings(chopSettings));
                }
        );
    }

}
