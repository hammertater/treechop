package ht.treechop.api;

import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.LazyTreeData;
import ht.tuber.graph.DirectedGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.BiPredicate;

public class TreeDetectorBuilder {
    private BiPredicate<Level, BlockPos> logMatcher;
    private BiPredicate<Level, BlockPos> leavesMatcher;
    private DirectedGraph<BlockPos> graph;

    public TreeDetectorBuilder() {
        logMatcher = ChopUtil::isBlockALog;
        leavesMatcher = ChopUtil::isBlockLeaves;
    }

    public TreeDetectorBuilder logs(BiPredicate<Level, BlockPos> logMatcher) {
        this.logMatcher = logMatcher;
        return this;
    }

    public TreeDetectorBuilder leaves(BiPredicate<Level, BlockPos> leavesMatcher) {
        this.leavesMatcher = leavesMatcher;
        return this;
    }

    public TreeDetectorBuilder scanner(DirectedGraph<BlockPos> pattern) {
        this.graph = pattern;
        return this;
    }

    public ITreeBlock build() {
        return null;
//        return (level, origin) -> new LazyTreeData(level) {
//
//        }
    }
}
