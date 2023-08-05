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
                .logs(HugeMushroomHandler::isStem)
                .leaves(HugeMushroomHandler::isCap)
                .leavesScanner((level, pos) -> BlockNeighbors.ADJACENTS_AND_BELOW.asStream(pos))
                .maxLeavesDistance(4)
                .build();

        HugeMushroomHandler handler = new HugeMushroomHandler();
        stems.get().forEach(block -> {
            api.overrideChoppableBlock(block, true);
            api.registerChoppableBlockBehavior(block, handler);
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

    private static final Lazy<Set<Block>> stems = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getIdentifiedBlocks(MyConfigHandler.instance.stemBlocksList.get()).collect(Collectors.toSet())
    );
    private static final Lazy<Set<Block>> caps = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getIdentifiedBlocks(MyConfigHandler.instance.capBlocksList.get()).collect(Collectors.toSet())
    );

    public static boolean isStem(Level level, BlockPos pos, BlockState state) {
        return stems.get().contains(state.getBlock());
    }

    public static boolean isCap(Level level, BlockPos pos, BlockState state) {
        return caps.get().contains(state.getBlock());
    }

    public static class MyConfigHandler {
        private static MyConfigHandler instance;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> stemBlocksList;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> capBlocksList;

        public MyConfigHandler(ForgeConfigSpec.Builder builder) {
            builder.push("hugeMushrooms");
            stemBlocksList = builder.defineList("logs", List.of(ConfigHandler.getCommonTagId("mushroom_stems")), always -> true);
            capBlocksList = builder.defineList("leaves", List.of(ConfigHandler.getCommonTagId("mushroom_caps")), always -> true);
            builder.pop();
        }

        public static void init(ForgeConfigSpec.Builder builder) {
            instance = new MyConfigHandler(builder);
        }
    }
}
