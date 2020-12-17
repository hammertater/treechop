package ht.treechop.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class ChopResult {
    public static final ChopResult IGNORED = new ChopResult(null, null);
    private final BlockPos blockPos;
    private final IBlockState blockState;

    public ChopResult(BlockPos choppedPos, IBlockState choppedState) {
        this.blockPos = choppedPos;
        this.blockState = choppedState;
    }

    public BlockPos getChoppedBlockPos() { return blockPos; }
    public IBlockState getChoppedBlockState() { return blockState; }
}
