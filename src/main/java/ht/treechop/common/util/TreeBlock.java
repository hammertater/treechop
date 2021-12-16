package ht.treechop.common.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TreeBlock extends WorldBlock {
    private final boolean chopped;

    public TreeBlock(Level level, BlockPos pos, BlockState state, boolean chopped) {
        super(level, pos, state);
        this.chopped = true;
    }

    public TreeBlock(Level level, BlockPos pos, BlockState state) {
        this(level, pos, state, false);
    }

    public boolean wasChopped() {
        return chopped;
    }
}
