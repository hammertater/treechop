package ht.treechop.common.network;

import ht.treechop.common.capabilities.ChopSettingsCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketEnableFelling implements IMessage {

    private boolean fellingEnabled;

    public PacketEnableFelling(boolean fellingEnabled) {
        this.fellingEnabled = fellingEnabled;
    }

    public PacketEnableFelling() {
        this(true);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        fellingEnabled = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(fellingEnabled);
    }

    public static class Handler implements IMessageHandler<PacketEnableFelling, IMessage> {
        @Override
        public IMessage onMessage(PacketEnableFelling message, MessageContext context) {
            if (context.side == Side.SERVER) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServer().addScheduledTask(() -> {
                    ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                    chopSettings.setFellingEnabled(message.fellingEnabled);
                    player.sendMessage(new TextComponentString(TextFormatting.GRAY + "[TreeChop] " + TextFormatting.WHITE + "Felling " + (message.fellingEnabled ? "ON" : "OFF")));
                });
            }
            return null;
        }
    }
}
