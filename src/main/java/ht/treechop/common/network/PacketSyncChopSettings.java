package ht.treechop.common.network;

import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.config.SneakBehavior;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class PacketSyncChopSettings implements IMessage {

    protected ChopSettings chopSettings;
    private static final SneakBehavior defaultSneakBehavior = new ChopSettings().getSneakBehavior();

    public PacketSyncChopSettings(ChopSettings chopSettings) {
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

}
