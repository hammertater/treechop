package ht.treechoptests.block;

import ht.treechop.api.ISimpleChoppableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SkinnyLog extends Block implements ISimpleChoppableBlock {
    public SkinnyLog(Properties properties) {
        super(properties);
    }

    @Override
    public int getUnchoppedRadius(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return 4;
    }
}
