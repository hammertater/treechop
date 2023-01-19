package ht.treechoptests;

import ht.treechop.api.ISimpleChoppableBlock;
import ht.treechop.api.TreeChopAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = TreeChopForgeTests.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SlimTreeTest {
    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) api -> {
            api.registerChoppableBlockBehavior(Blocks.OAK_LOG, new ISimpleChoppableBlock() {
                @Override
                public int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState) {
                    return 4;
                }
            });
        });
    }
}
