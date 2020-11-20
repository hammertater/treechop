package ht.treechop;

import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.init.ModBlocks;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

@Mod("treechop")
public class TreeChopMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "treechop";
    public static boolean breakLeaves = true;
    public static int maxTreeSize = 256;

    public TreeChopMod() {
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent event) {
        if (event.getPlayer().isSneaking())
            return;

        BlockState blockState = event.getState();
        IWorld world = event.getWorld();
        BlockPos blockPos = event.getPos();

        int numChops;
        if (!(blockState.getBlock() instanceof ChoppedLogBlock) && isBlockChoppable(world, blockPos, blockState)) {
            blockState = ChopUtil.chipBlock(world, blockPos, 1, event.getPlayer());
            numChops = 0;
        } else {
            numChops = 1;
        }

        if (blockState.getBlock() instanceof ChoppedLogBlock) {
            ChoppedLogBlock block = (ChoppedLogBlock) blockState.getBlock();
            block.chop(world, blockPos, blockState, event.getPlayer(), numChops);
            event.setCanceled(true);
        }
    }
}
