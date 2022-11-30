package ht.treechoptests.mixin;

import ht.treechop.api.ISimpleChoppableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HugeMushroomBlock.class)
public class HugeMushroomBlockMixin implements ISimpleChoppableBlock {
    @Override
    public int getRadius(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return 4;
    }
}
