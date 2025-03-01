package ht.treechop.compat;

import ht.treechop.TreeChop;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TheOneProbe {
    @SubscribeEvent
    public static void enqueueModComms(InterModEnqueueEvent event) {
        InterModComms.sendTo(
                "theoneprobe",
                "getTheOneProbe",
                () -> (Function<Object, Void>) probe -> TheOneProbeInfoProvider.register((ITheOneProbe) probe)
        );
    }

}