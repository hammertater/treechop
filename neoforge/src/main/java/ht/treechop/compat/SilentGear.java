package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppingItem;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Consumer;

/**
 * Make saws perform several chops instead of 1.
 */
@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class SilentGear {
    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ConfigHandler.COMMON.compatForSilentGear.get() && ModList.get().isLoaded("silentgear")) {
            InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) api -> {
                Item saw = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("silentgear", "saw"));
                if (saw != null) {
                    api.registerChoppingItemBehavior(saw, new IChoppingItem() {
                        @Override
                        public boolean canChop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState target) {
                            return true;
                        }

                        @Override
                        public int getNumChops(ItemStack tool, BlockState target) {
                            return ConfigHandler.COMMON.silentGearSawChops.get();
                        }
                    });
                }
            });
        }
    }
}
