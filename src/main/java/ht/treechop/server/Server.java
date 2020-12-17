package ht.treechop.server;

import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Server {

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            EntityPlayer oldPlayer = event.getOriginal();
            EntityPlayer newPlayer = event.getEntityPlayer();
            ChopSettings oldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
            ChopSettings newSettings = ChopSettingsCapability.forPlayer(newPlayer);
            newSettings.copyFrom(oldSettings);
        }
    }
}
