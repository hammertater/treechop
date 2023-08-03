package ht.treechop.api;

import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.LazyTreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public class TreeDetectorBuilder {
    private BlockMatcher logMatcher = ChopUtil::isBlockALog;
    private BlockMatcher leavesMatcher = ChopUtil::isBlockLeaves;
    private BiFunction<Level, BlockPos, Stream<BlockPos>> treeScanner = ((level, pos) -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(pos));
    private BiFunction<Level, BlockPos, Stream<BlockPos>> leavesScanner = ((level, pos) -> BlockNeighbors.ADJACENTS.asStream(pos));
    private int maxLogs;
    private int maxLeavesDistance;

    public TreeDetectorBuilder() {
        maxLogs = ConfigHandler.COMMON.maxNumTreeBlocks.get();
        maxLeavesDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
    }
    public TreeDetectorBuilder logs(BlockMatcher logMatcher) {
        this.logMatcher = logMatcher;
        return this;
    }

    public TreeDetectorBuilder leaves(BlockMatcher leavesMatcher) {
        this.leavesMatcher = leavesMatcher;
        return this;
    }

    public TreeDetectorBuilder treeScanner(BiFunction<Level, BlockPos, Stream<BlockPos>> scanner) {
        this.treeScanner = scanner;
        return this;
    }

    public TreeDetectorBuilder leavesScanner(BiFunction<Level, BlockPos, Stream<BlockPos>> scanner) {
        this.leavesScanner = scanner;
        return this;
    }

    public TreeDetectorBuilder maxLogCount(int maxLogs) {
        this.maxLogs = maxLogs;
        return this;
    }

    /**
     * @param maxLeavesDistance the maximum shortest-path distance that leaves can be from the tree trunk. Set to 0 to use smart leaves detection (only break adjacent leaves that have a lower "distance" property).
     */
    public TreeDetectorBuilder maxLeavesDistance(int maxLeavesDistance) {
        this.maxLeavesDistance = maxLeavesDistance;
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
                maxLeavesDistance
        );
    }
}
