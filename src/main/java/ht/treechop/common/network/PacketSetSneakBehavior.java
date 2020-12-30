package ht.treechop.common.network;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.SneakBehavior;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSetSneakBehavior implements IMessage {

    private SneakBehavior sneakBehavior;
    private static final SneakBehavior defaultSneakBehavior = new ChopSettings().getSneakBehavior();

    public PacketSetSneakBehavior(SneakBehavior sneakBehavior) {
        this.sneakBehavior = sneakBehavior;
    }

    public PacketSetSneakBehavior() {
        this(defaultSneakBehavior);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int sneakBehaviorIndex = buffer.readInt();
        sneakBehavior = (sneakBehaviorIndex < SneakBehavior.values().length) ? SneakBehavior.values()[sneakBehaviorIndex] : defaultSneakBehavior;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(sneakBehavior.ordinal());
    }

    public static class Handler implements IMessageHandler<PacketSetSneakBehavior, IMessage> {
        @Override
        public IMessage onMessage(PacketSetSneakBehavior message, MessageContext context) {
            if (context.side == Side.SERVER) {
                EntityPlayerMP player = context.getServerHandler().player;
                player.getServer().addScheduledTask(() -> {
                    if (ConfigHandler.canChooseNotToChop) {
                        ChopSettingsCapability chopSettings = ChopSettingsCapability.forPlayer(player);
                        chopSettings.setSneakBehavior(message.sneakBehavior);
                        player.sendMessage(TreeChopMod.makeText("Sneak behavior " + chopSettings.getSneakBehavior().getName()));
                    } else {
                        player.sendMessage(TreeChopMod.makeText("Sneak behavior " + SneakBehavior.NONE.getName() + TextFormatting.RED + " (you are not permitted to disable chopping or felling)"));
                    }
                });
            }
            return null;
        }
    }
}
