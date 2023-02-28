package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheOneProbe {
    @SubscribeEvent
    public static void enqueueModComms(InterModEnqueueEvent event) {
        if (ConfigHandler.COMMON.compatForTheOneProbe.get()) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<Object, Void>) probe -> TheOneProbeInfoProvider.register((ITheOneProbe) probe));
        }
    }
}
