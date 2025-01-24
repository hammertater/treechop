package ht.treechop.compat;

import ht.treechop.api.ITreeBlock;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.api.TreeData;
import ht.treechop.api.TreeDetectorBuilder;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HugeFungusHandler implements ITreeBlock {
    private static ITreeBlock detectionHandler;

    public static void register(TreeChopAPI api) {
        detectionHandler = new TreeDetectorBuilder()
                .logs(HugeFungusHandler::isLog)
                .leaves(HugeFungusHandler::isLeaves)
                .maxLeavesDistance(6)
                .build();

        HugeFungusHandler handler = new HugeFungusHandler();
        logs.get().forEach(block -> {
            api.overrideChoppableBlock(block, true);
            api.registerBlockBehavior(block, handler);
        });
        leaves.get().forEach(block -> {
            api.overrideLeavesBlock(block, true);
        });
    }

    @Override
    public TreeData getTree(Level level, BlockPos origin) {
        return detectionHandler.getTree(level, origin);
    }

    private static final Lazy<Set<Block>> logs = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getIdentifiedBlocks(MyConfigHandler.instance.logIds.get()).collect(Collectors.toSet())
    );
    private static final Lazy<Set<Block>> leaves = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getIdentifiedBlocks(MyConfigHandler.instance.leavesIds.get()).collect(Collectors.toSet())
    );

    public static boolean isLog(Level level, BlockPos pos, BlockState state) {
        return logs.get().contains(state.getBlock());
    }

    public static boolean isLeaves(Level level, BlockPos pos, BlockState state) {
        return leaves.get().contains(state.getBlock());
    }

    public static class MyConfigHandler {
        private static MyConfigHandler instance;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> logIds;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> leavesIds;

        public MyConfigHandler(ForgeConfigSpec.Builder builder) {
            builder.push("hugeFungi");
            logIds = builder.defineList("logs", List.of(
                    "#minecraft:crimson_stems",
                    "#minecraft:warped_stems"
            ), always -> true);
            leavesIds = builder.defineList("leaves", List.of(
                    "#minecraft:wart_blocks",
                    "minecraft:shroomlight"
            ), always -> true);
            builder.pop();
        }

        public static void init(ForgeConfigSpec.Builder builder) {
            instance = new MyConfigHandler(builder);
        }
    }
}
