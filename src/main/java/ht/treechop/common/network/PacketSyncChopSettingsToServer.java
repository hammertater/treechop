package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncChopSettingsToServer extends PacketSyncChopSettings {

    public PacketSyncChopSettingsToServer(ChopSettings chopSettings) {
        super(chopSettings);
    }

    public PacketSyncChopSettingsToServer() {
        super();
    }

    public static class Handler implements IMessageHandler<PacketSyncChopSettingsToServer, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncChopSettingsToServer message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> {
                ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);

                if (!chopSettings.isSynced()) {
                    TreeChopMod.LOGGER.info("Received chop settings from player " + player.getDisplayNameString());
                    chopSettings.copyFrom(message.chopSettings);
                    chopSettings.setSynced();
                }

                // Force settings through that aren't yet configurable in-game
                chopSettings.setOnlyChopTreesWithLeaves(message.chopSettings.getTreeMustHaveLeaves());

                TreeChopMod.LOGGER.info("Sending chop settings to player " + player.getDisplayNameString());
                PacketHandler.sendTo(player, new PacketSyncChopSettingsToClient(chopSettings));
            });
            return null;
        }
    }

}
