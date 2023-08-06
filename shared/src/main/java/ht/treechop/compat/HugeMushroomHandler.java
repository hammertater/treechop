package ht.treechop.compat;

import ht.treechop.api.*;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import ht.treechop.common.util.BlockNeighbors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HugeMushroomHandler implements IStrippableBlock, ITreeBlock {
    private static ITreeBlock detectionHandler;

    public static void register(TreeChopAPI api) {
        detectionHandler = new TreeDetectorBuilder()
                .logs(HugeMushroomHandler::isLog)
                .leaves(HugeMushroomHandler::isLeaves)
                .leavesScanner((level, pos) -> BlockNeighbors.ADJACENTS_AND_BELOW.asStream(pos))
                .maxLeavesDistance(5)
                .build();

        HugeMushroomHandler handler = new HugeMushroomHandler();
        logs.get().forEach(block -> {
            api.overrideChoppableBlock(block, true);
            api.registerChoppableBlockBehavior(block, handler);
        });
        leaves.get().forEach(block -> {
            api.overrideLeavesBlock(block, true);
        });
    }

    @Override
    public BlockState getStrippedState(BlockGetter level, BlockPos pos, BlockState blockState) {
        if (PipeBlock.PROPERTY_BY_DIRECTION.values().stream().anyMatch(property -> !blockState.hasProperty(property))) {
            return blockState;
        } else {
            return blockState
                    .setValue(PipeBlock.NORTH, false)
                    .setValue(PipeBlock.EAST, false)
                    .setValue(PipeBlock.SOUTH, false)
                    .setValue(PipeBlock.WEST, false)
                    .setValue(PipeBlock.UP, false)
                    .setValue(PipeBlock.DOWN, false);
        }
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
            builder.push("hugeMushrooms");
            logIds = builder.defineList("logs", List.of(ConfigHandler.getCommonTagId("mushroom_stems")), always -> true);
            leavesIds = builder.defineList("leaves", List.of(ConfigHandler.getCommonTagId("mushroom_caps")), always -> true);
            builder.pop();
        }

        public static void init(ForgeConfigSpec.Builder builder) {
            instance = new MyConfigHandler(builder);
        }
    }
}
