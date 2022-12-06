package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeChopAPI;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Consumer;

/**
 * This is totally unnecessary - just testing the API stuff
  */
@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID, bus = Bus.MOD)
public class TreeChopAPITest {

    private static TreeChopAPI api = null;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        Bus.FORGE.bus().get().addListener(TreeChopAPITest::onTagsUpdated);
    }

    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) response -> {
            api = response;
        } );
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (api != null) {
            TreeChop.initUsingAPI(api);
        }
    }
}
