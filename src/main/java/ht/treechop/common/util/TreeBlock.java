package ht.treechop.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeBlock extends WorldBlock {
    private final boolean chopped;

    public TreeBlock(World world, BlockPos pos, BlockState state, boolean chopped) {
        super(world, pos, state);
        this.chopped = true;
    }

    public TreeBlock(World world, BlockPos pos, BlockState state) {
        this(world, pos, state, false);
    }

    public boolean wasChopped() {
        return chopped;
    }
}
