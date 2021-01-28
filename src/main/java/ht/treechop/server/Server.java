package ht.treechop.server;

import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

public class Server {

    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Server::onPlayerCloned);
    }

    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            PlayerEntity oldPlayer = event.getOriginal();
            PlayerEntity newPlayer = event.getPlayer();
            LazyOptional<ChopSettingsCapability> lazyOldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
            LazyOptional<ChopSettingsCapability> lazyNewSettings = ChopSettingsCapability.forPlayer(newPlayer);

            lazyOldSettings.ifPresent(
                    oldSettings -> lazyNewSettings.ifPresent(
                            newSettings -> newSettings.copyFrom(oldSettings)
                    )
            );
        }
    }

}
