package ht.treechop.compat;

//
//public abstract class MushroomDetection {
//    static TreeData detectHugeShrooms(Level level, BlockPos origin) {
////        if (isStem(level, origin) && treeData instanceof LazyTreeData lazyTree) {
////            return new LazyTreeData(
////                    lazyTree.getLevel(),
////                    lazyTree.getIncompleteLogs(),
////                    BlockNeighbors.HORIZONTAL_AND_ABOVE::asStream,
////                    pos -> isStem(level, pos),
////                    pos -> isCap(level, pos),
////                    ConfigHandler.COMMON.maxNumTreeBlocks.get()
////            ) {
////                @Override
////                public Stream<BlockPos> streamLeaves() {
////                    // Discover leaves adjacent to cap
////                    streamLogs().forEach(a -> {});
////                    int breakDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
////
////                    DirectedGraph<BlockPos> cap = GraphUtil.filter(
////                            BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS::asStream,
////                            pos -> isCap(level, pos) && ChopUtil.horizontalBlockDistance(origin, pos) < breakDistance
////                    );
////
////                    return GraphUtil.flood(cap, getIncompleteLeaves())
////                            .fill()
////                            .limit(ConfigHandler.COMMON.maxNumLeavesBlocks.get());
////                }
////            };
////        }
//
////        return treeData;
////    }
//}
