package ht.treechop.capabilities;

import ht.treechop.config.SneakBehavior;
import ht.treechop.network.PacketEnableChopping;
import ht.treechop.network.PacketEnableFelling;
import ht.treechop.network.PacketHandler;
import ht.treechop.network.PacketSetSneakBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class ChopSettings {

    private boolean isSynced = false;
    private boolean choppingEnabled = true;
    private boolean fellingEnabled = true;
    private SneakBehavior sneakBehavior = SneakBehavior.INVERT_CHOPPING;

    public ChopSettings() {}

    public ChopSettings(boolean choppingEnabled, boolean fellingEnabled, SneakBehavior sneakBehavior) {
        this.choppingEnabled = choppingEnabled;
        this.fellingEnabled = fellingEnabled;
        this.sneakBehavior = sneakBehavior;
    }

    public boolean getChoppingEnabled() { return choppingEnabled; }
    public boolean getFellingEnabled() { return fellingEnabled; }
    public SneakBehavior getSneakBehavior() { return sneakBehavior; }

    public void setChoppingEnabled(boolean enabled) { choppingEnabled = enabled; }
    public void setFellingEnabled(boolean enabled) { fellingEnabled = enabled; }
    public void setSneakBehavior(SneakBehavior behavior) { sneakBehavior = behavior; }

    public void toggleChopping() {
        setChoppingEnabled(!choppingEnabled);
    }

    public void toggleFelling() {
        setFellingEnabled(!fellingEnabled);
    }

    public void cycleSneakBehavior() {
        SneakBehavior nextSneakBehavior = SneakBehavior.values()[Math.floorMod(sneakBehavior.ordinal() + 1, SneakBehavior.values().length)];
        setSneakBehavior(nextSneakBehavior);
    }

    public void copyFrom(ChopSettings oldSettings) {
        this.choppingEnabled = oldSettings.choppingEnabled;
        this.fellingEnabled = oldSettings.fellingEnabled;
        this.sneakBehavior = oldSettings.sneakBehavior;
    }

}
