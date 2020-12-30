package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketEnableChopping implements IMessage {

    private boolean choppingEnabled;

    public PacketEnableChopping(boolean choppingEnabled) {
        this.choppingEnabled = choppingEnabled;
    }

    public PacketEnableChopping() {
        this(true);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        choppingEnabled = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(choppingEnabled);
    }

    public static class Handler implements IMessageHandler<PacketEnableChopping, IMessage> {
        @Override
        public IMessage onMessage(PacketEnableChopping message, MessageContext context) {
            if (context.side == Side.SERVER) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServer().addScheduledTask(() -> {
                    if (ConfigHandler.canChooseNotToChop) {
                        ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                        chopSettings.setChoppingEnabled(message.choppingEnabled);
                        player.sendMessage(TreeChopMod.makeText("Chopping " + (chopSettings.getChoppingEnabled() ? "ON" : "OFF")));
                    } else {
                        player.sendMessage(TreeChopMod.makeText("Chopping ON" + TextFormatting.RED + " (you are not permitted to disable chopping)"));
                    }
                });
            }
            return null;
        }
    }
}
