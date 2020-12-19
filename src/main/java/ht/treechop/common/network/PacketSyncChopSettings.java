package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.SneakBehavior;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSyncChopSettings implements IMessage {

    private ChopSettings chopSettings;
    private static final SneakBehavior defaultSneakBehavior = new ChopSettings().getSneakBehavior();

    public  PacketSyncChopSettings(ChopSettings chopSettings) {
        this.chopSettings = chopSettings;
    }

    public PacketSyncChopSettings() {
        this(new ChopSettings());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        chopSettings.setChoppingEnabled(buffer.readBoolean());
        chopSettings.setFellingEnabled(buffer.readBoolean());

        int sneakBehaviorIndex = buffer.readInt();
        chopSettings.setSneakBehavior(sneakBehaviorIndex < SneakBehavior.values().length ? SneakBehavior.values()[sneakBehaviorIndex] : defaultSneakBehavior);

        chopSettings.setOnlyChopTreesWithLeaves(buffer.readBoolean());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(chopSettings.getChoppingEnabled());
        buffer.writeBoolean(chopSettings.getFellingEnabled());
        buffer.writeInt(chopSettings.getSneakBehavior().ordinal());
        buffer.writeBoolean(chopSettings.getOnlyChopTreesWithLeaves());
    }

    public static class Handler implements IMessageHandler<PacketSyncChopSettings, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncChopSettings message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServer().addScheduledTask(() -> {
                if (context.side == Side.SERVER) {
                    handleOnServer(message, context);
                } else {
                    handleOnClient(message);
                }
            });
            return null;
        }

        private static void handleOnClient(PacketSyncChopSettings message) {
            TreeChopMod.LOGGER.info("Received chop settings from server");
            ChopSettings chopSettings = Client.getChopSettings();
            chopSettings.copyFrom(message.chopSettings);
        }

        public static void handleOnServer(PacketSyncChopSettings message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);

            if (!chopSettings.isSynced()) {
                TreeChopMod.LOGGER.info("Received chop settings from player " + player.getDisplayNameString());
                chopSettings.copyFrom(message.chopSettings);
                chopSettings.setSynced();
            }

            // Force settings through that aren't yet configurable in-game
            chopSettings.setOnlyChopTreesWithLeaves(message.chopSettings.getOnlyChopTreesWithLeaves());

            TreeChopMod.LOGGER.info("Sending chop settings to player " + player.getDisplayNameString());
            PacketHandler.sendTo(player, new PacketSyncChopSettings(chopSettings));
        }
    }

}
