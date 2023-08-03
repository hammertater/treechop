package ht.treechop.compat;

import ht.treechop.api.TreeData;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.LazyTreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import ht.treechop.common.util.BlockNeighbors;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MushroomCapDetection {
    private static final Lazy<Set<Block>> stems = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getMushroomStems().collect(Collectors.toSet())
    );
    private static final Lazy<Set<Block>> caps = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getMushroomCaps().collect(Collectors.toSet())
    );

    private static boolean isStem(Level level, BlockPos pos) {
        return stems.get().contains(ChopUtil.getLogBlock(level, pos));
    }

    private static boolean isCap(Level level, BlockPos pos) {
        return caps.get().contains(level.getBlockState(pos).getBlock());
    }

    static TreeData detectHugeShrooms(Level level, BlockPos origin, TreeData treeData) {
//        if (isStem(level, origin) && treeData instanceof LazyTreeData lazyTree) {
//            return new LazyTreeData(
//                    lazyTree.getLevel(),
//                    lazyTree.getIncompleteLogs(),
//                    BlockNeighbors.HORIZONTAL_AND_ABOVE::asStream,
//                    pos -> isStem(level, pos),
//                    pos -> isCap(level, pos),
//                    ConfigHandler.COMMON.maxNumTreeBlocks.get()
//            ) {
//                @Override
//                public Stream<BlockPos> streamLeaves() {
//                    // Discover leaves adjacent to cap
//                    streamLogs().forEach(a -> {});
//                    int breakDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
//
//                    DirectedGraph<BlockPos> cap = GraphUtil.filter(
//                            BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS::asStream,
//                            pos -> isCap(level, pos) && ChopUtil.horizontalBlockDistance(origin, pos) < breakDistance
//                    );
//
//                    return GraphUtil.flood(cap, getIncompleteLeaves())
//                            .fill()
//                            .limit(ConfigHandler.COMMON.maxNumLeavesBlocks.get());
//                }
//            };
//        }

        return treeData;
    }
}
