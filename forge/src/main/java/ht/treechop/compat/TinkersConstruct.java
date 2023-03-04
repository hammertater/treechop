package ht.treechop.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.definition.aoe.IAreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Disable AOE breaking behavior when chopping. Instead, make broad axes perform 5 * (expanded level) chops and other wood-cutting tools perform 1 + (expanded level) chops.
 */
@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TinkersConstruct {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForTinkersConstruct.get() && ModList.get().isLoaded("tconstruct")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        private static double treeAoeChops() {
            return ConfigHandler.COMMON.tinkersConstructTreeAOEChops.get();
        }

        private static double woodAoeChops() {
            return ConfigHandler.COMMON.tinkersConstructWoodAOEChops.get();
        }

        private static double expandedMultiplier() {
            return ConfigHandler.COMMON.tinkersConstructExpandedMultiplier.get();
        }

        @SubscribeEvent
        public static void onChop(ChopEvent.StartChopEvent event) {
            ItemStack tool = event.getPlayer().getMainHandItem();
            if (tool.getItem() instanceof ModifiableItem) {
                ToolStack toolStack = ToolStack.from(tool);
                int bonusChops = getNumChops(toolStack) - 1;
                if (bonusChops > 0) {
                    event.setNumChops(event.getNumChops() + bonusChops);
                }
            }
        }

        private static int getNumChops(ToolStack toolStack) {
            final ModifierId expandedId = new ModifierId("tconstruct", "expanded");
            double level = toolStack.getModifierLevel(expandedId);

            if ("tconstruct:tree".equals(getAoeId(toolStack))) {
                return (int) (treeAoeChops() * (1.0 + level * expandedMultiplier()));
            } else {
                return (int) (woodAoeChops() * (1.0 + level * expandedMultiplier()));
            }
        }

        private static String getAoeId(ToolStack toolStack) {
            try {
                JsonElement genericData = IAreaOfEffectIterator.LOADER.serialize(toolStack.getDefinition().getData().getAOE());
                if (genericData.isJsonObject()) {
                    JsonObject data = genericData.getAsJsonObject();
                    String aoeId = getJsonStringOrEmpty(data, "type");
                    if (aoeId.equals("tconstruct:fallback")) {
                        data = data.getAsJsonObject("if_matches");
                        if (data != null) {
                            return getJsonStringOrEmpty(data, "type");
                        }
                    } else {
                        return aoeId;
                    }
                }
            } catch (UnsupportedOperationException ignored) {
            }

            return "";
        }

        private static String getJsonStringOrEmpty(JsonObject json, String field) {
            JsonElement element = json.get(field);
            return (element != null) ? element.getAsString() : "";
        }
    }
}
