package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeChopAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This is totally unnecessary - just testing the API
 */
public class TreeChopFabricAPITest {

    private static TreeChopAPI api = null;

    public static void init() {
        api = (TreeChopAPI) FabricLoader.getInstance().getObjectShare().get("treechop:api");
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> TreeChopFabricAPITest.onTagsUpdated());
    }

    public static void onTagsUpdated() {
        if (api != null) {
            TreeChop.initUsingAPI(api);
        }
    }
}
