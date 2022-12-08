package ht.treechop.mixin;

import com.terraformersmc.terraform.wood.block.BareSmallLogBlock;
import ht.treechop.api.ISimpleChoppableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Unique(silent = true)
@Mixin(BareSmallLogBlock.class)
public class TerraformTreesMixin implements ISimpleChoppableBlock {
    @Override
    public int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState) {
        return 5;
    }
}
