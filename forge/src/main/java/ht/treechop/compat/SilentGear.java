package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppingItem;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * Make saws perform several chops instead of 1.
 */
@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SilentGear {
    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ConfigHandler.COMMON.compatForSilentGear.get() && ModList.get().isLoaded("silentgear")) {
            InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) api -> {
                Item saw = ForgeRegistries.ITEMS.getValue(new ResourceLocation("silentgear", "saw"));
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
