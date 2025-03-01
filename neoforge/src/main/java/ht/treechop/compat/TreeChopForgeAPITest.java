package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeChopAPI;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.function.Consumer;

/**
 * This is totally unnecessary - just testing the API
 */
@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TreeChopForgeAPITest {

    private static TreeChopAPI api = null;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(TreeChopForgeAPITest::onTagsUpdated);
    }

    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) response -> {
            api = response;
        });
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (api != null) {
            TreeChop.initUsingAPI(api);
        }
    }
}
