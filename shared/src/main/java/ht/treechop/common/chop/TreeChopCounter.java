package ht.treechop.common.chop;

import ht.treechop.api.TreeData;
import ht.treechop.common.config.ChopCountingAlgorithm;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

public class TreeChopCounter {

    private final TreeData tree;
    private int count;
    private ChopCountingAlgorithm algo;

    public TreeChopCounter(Level level, BlockPos target, TreeData tree, ChopCountingAlgorithm algo) {
        count = ChopUtil.getNumChops(level, target);
        this.tree = tree;
        this.algo = algo;
    }

    public boolean isEnoughToFell(int extraChops) {
        AtomicInteger blockCount = new AtomicInteger(0);
        int numChops = count + extraChops;
        int supNumBlocks = (int) Math.ceil(algo.inverse(numChops + 1));
        // BAD BAD BAD
        long numBlocksLowerBound = tree.streamLogBlocks().takeWhile(ignored -> blockCount.getAndIncrement());
        if (numBlocksLowerBound > supNumBlocks) {
            return true;
        }

        return false;
    }
}
