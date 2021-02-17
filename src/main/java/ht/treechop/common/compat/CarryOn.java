package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CarryOn {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForCarryOn.get() && ModList.get().isLoaded("carryon")) {
            TreeChopMod.LOGGER.info("Disabling chopping during right-click actions to fix conflicts with Carry On (please update Carry On to carryon-1.16.5-1.15.2.9 or later for better compatibility)");
            NoChopOnRightClick.enable();
        }
    }

}
