package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncChopSettingsToClient extends PacketSyncChopSettings {

    public PacketSyncChopSettingsToClient(ChopSettings chopSettings) {
        super(chopSettings);
    }

    public PacketSyncChopSettingsToClient() {
        super();
    }

    public static class Handler implements IMessageHandler<PacketSyncChopSettingsToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncChopSettingsToClient message, MessageContext context) {
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> {
                TreeChopMod.LOGGER.info("Received chop settings from server");
                ChopSettings chopSettings = Client.getChopSettings();
                chopSettings.copyFrom(message.chopSettings);
            });
            return null;
        }
    }

}
