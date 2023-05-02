package ht.treechop.common.util;

import ht.treechop.api.TreeData;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TreeCache {
    private final SingleBlockCache<TreeData> singleBlockCache = new SingleBlockCache<>();

    public TreeData getTree(Level level, BlockPos pos, int maxNumTreeBlocks) {
        TreeData tree = singleBlockCache.get(level, pos);
        if (tree == null) {
            tree = ChopUtil.getTree(level, pos, maxNumTreeBlocks);
            singleBlockCache.put(level, pos, tree);
        }

        return tree;
    }

    public void invalidate() {
        singleBlockCache.invalidate();
    }

    private static class SingleBlockCache<T> {
        private T value;
        private BlockGetter level;
        private BlockState blockState;
        private BlockPos pos;

        public T get(BlockGetter level, BlockPos pos) {
            if (level == this.level && pos.equals(this.pos) && level.getBlockState(pos).equals(blockState)) {
                return value;
            } else {
                return null;
            }
        }

        public void put(BlockGetter level, BlockPos pos, T value) {
            this.value = value;
            this.level = level;
            this.pos = pos;
            blockState = level.getBlockState(pos);
        }

        public void clear(BlockGetter level, BlockPos pos) {
            if (level == this.level & pos.equals(this.pos)) {
                value = null;
                blockState = Blocks.AIR.defaultBlockState();
            }
        }

        public void invalidate() {
            level = null;
            value = null;
            blockState = Blocks.AIR.defaultBlockState();
        }
    }
}
