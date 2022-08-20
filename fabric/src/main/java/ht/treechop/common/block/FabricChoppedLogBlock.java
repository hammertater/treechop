package ht.treechop.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FabricChoppedLogBlock extends ChoppedLogBlock {

    public FabricChoppedLogBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MyEntity(blockPos, blockState);
    }

    public static class MyEntity extends ChoppedLogBlock.MyEntity {
        public MyEntity(BlockPos pos, BlockState blockState) {
            super(pos, blockState);
        }
    }

}
