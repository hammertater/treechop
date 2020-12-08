package ht.treechop.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class ChopResult {
    public static final ChopResult IGNORED = new ChopResult(null, null);
    private final BlockPos blockPos;
    private final BlockState blockState;

    public ChopResult(BlockPos choppedPos, BlockState choppedState) {
        this.blockPos = choppedPos;
        this.blockState = choppedState;
    }

    public BlockPos getChoppedBlockPos() { return blockPos; }
    public BlockState getChoppedBlockState() { return blockState; }
}
