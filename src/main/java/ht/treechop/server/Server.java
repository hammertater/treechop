package ht.treechop.server;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class Server {

    @SubscribeEvent
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
