package ht.treechop.api;

import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.LazyTreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Builds specialized {@link ITreeBlock} handlers for detecting non-standard trees. Enable block handlers using {@link TreeChopAPI#registerBlockBehavior}.
 */
public class TreeDetectorBuilder {
    private BlockMatcher logMatcher = ChopUtil::isBlockALog;
    private BlockMatcher leavesMatcher = ChopUtil::isBlockLeaves;
    private BiFunction<Level, BlockPos, Stream<BlockPos>> treeScanner = ((level, pos) -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(pos));
    private BiFunction<Level, BlockPos, Stream<BlockPos>> leavesScanner = ((level, pos) -> BlockNeighbors.ADJACENTS.asStream(pos));
    private int maxLogs;
    private int maxLeavesDistance;
    private LeavesStrategy leavesStrategy = LeavesStrategy.SMART;

    public TreeDetectorBuilder() {
        maxLogs = ConfigHandler.COMMON.maxNumTreeBlocks.get();
        maxLeavesDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
    }

    /**
     * @param logMatcher A function that returns true if a given block should be considered a log.
     */
    public TreeDetectorBuilder logs(BlockMatcher logMatcher) {
        this.logMatcher = logMatcher;
        return this;
    }

    /**
     * @param leavesMatcher A function that returns true if a given block should be considered leaves.
     */
    public TreeDetectorBuilder leaves(BlockMatcher leavesMatcher) {
        this.leavesMatcher = leavesMatcher;
        return this;
    }

    /**
     * @param scanner For a given log block's {@link BlockPos}, return a stream of positions to check for connected logs and leaves.
     */
    public TreeDetectorBuilder treeScanner(BiFunction<Level, BlockPos, Stream<BlockPos>> scanner) {
        this.treeScanner = scanner;
        return this;
    }

    /**
     * @param scanner For a given leaves block's {@link BlockPos}, return a stream of positions to check for connected leaves.
     */
    public TreeDetectorBuilder leavesScanner(BiFunction<Level, BlockPos, Stream<BlockPos>> scanner) {
        this.leavesScanner = scanner;
        return this;
    }

    /**
     * @param maxLogs The maximum number of logs the tree can have. Any logs beyond this count will be ignored. Does not affect leaves.
     */
    public TreeDetectorBuilder maxLogCount(int maxLogs) {
        this.maxLogs = maxLogs;
        return this;
    }

    /**
     * @param maxLeavesDistance the maximum shortest-path distance that leaves can be from the tree trunk. If unspecified, use what's in the treechop-common config.
     */
    public TreeDetectorBuilder maxLeavesDistance(int maxLeavesDistance) {
        this.maxLeavesDistance = maxLeavesDistance;
        return this;
    }

    /**
     * Set the {@link LeavesStrategy} used to find and break leaves when a tree is felled.
     * <l>
     * <li>{@link LeavesStrategy#SMART SMART}: based on each block's {@link net.minecraft.world.level.block.state.properties.BlockStateProperties#DISTANCE DISTANCE} value, if present, otherwise fallback to {@link LeavesStrategy#SHORTEST_PATH SHORTEST_PATH}</li>
     * <li>{@link LeavesStrategy#SHORTEST_PATH SHORTEST_PATH}: based on shortest-path distances through connected leaves, starting from log blocks</li>
     * </l>
     */
    public TreeDetectorBuilder leavesStrategy(LeavesStrategy strategy) {
        this.leavesStrategy = strategy;
        return this;
    }

    public ITreeBlock build() {
        return (level, origin) -> new LazyTreeData(
                level,
                origin,
                pos -> treeScanner.apply(level, pos),
                pos -> leavesScanner.apply(level, pos),
                pos -> logMatcher.matches(level, pos, ChopUtil.getLogState(level, pos)),
                pos -> leavesMatcher.matches(level, pos, level.getBlockState(pos)),
                maxLogs,
                maxLeavesDistance,
                leavesStrategy == LeavesStrategy.SMART
        );
    }

    public enum LeavesStrategy {
        SMART,
        SHORTEST_PATH
    }
}
