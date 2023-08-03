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

import java.util.Set;
import java.util.stream.Collectors;

public class FungusStemHandler implements IStrippableBlock, ITreeBlock {
    private static ITreeBlock detectionHandler;

    public static void register(TreeChopAPI api) {
        detectionHandler = new TreeDetectorBuilder()
                .logs(FungusStemHandler::isStem)
                .leaves(FungusStemHandler::isCap)
                .leavesScanner((level, pos) -> BlockNeighbors.ADJACENTS_AND_BELOW.asStream(pos))
                .maxLeavesDistance(4)
                .build();

        FungusStemHandler handler = new FungusStemHandler();
        ConfigHandler.getMushroomStems().forEach(block -> api.registerChoppableBlockBehavior(block, handler));
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
            () -> ConfigHandler.getMushroomStems().collect(Collectors.toSet())
    );
    private static final Lazy<Set<Block>> caps = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getMushroomCaps().collect(Collectors.toSet())
    );

    public static boolean isStem(Level level, BlockPos pos, BlockState state) {
        return stems.get().contains(state.getBlock());
    }

    public static boolean isCap(Level level, BlockPos pos, BlockState state) {
        return caps.get().contains(state.getBlock());
    }
}
