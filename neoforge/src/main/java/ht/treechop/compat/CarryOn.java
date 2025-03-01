package ht.treechop.compat;

import ht.treechop.TreeChop;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CarryOn {

    // Note: as of carryon 1.18.1.2, carryon is not receiving IMCs.
    @SubscribeEvent
    public static void enqueueModComms(InterModEnqueueEvent event) {
        InterModComms.sendTo("carryon", "blacklistBlock", () -> "treechop:chopped_log");
    }

}
