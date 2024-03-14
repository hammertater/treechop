package ht.treechop.compat;

import com.terraformersmc.terraform.wood.block.BareSmallLogBlock;
import ht.treechop.api.ISimpleChoppableBlock;
import ht.treechop.api.ITreeChopBlockBehavior;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class Terraformers implements ISimpleChoppableBlock {
    public static void init(TreeChopAPI api) {
        if (ConfigHandler.COMMON.compatForTerraformers.get()) {
            try {
                ITreeChopBlockBehavior handler = new ISimpleChoppableBlock() {
                    @Override
                    public int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState) {
                        if (blockState.getBlock() instanceof BareSmallLogBlock smallBlock) {
                            return smallBlock.getLogRadius();
                        } else {
                            return 8;
                        }
                    }
                };

                ConfigHandler.COMMON.choppableBlocks.get().stream()
                        .filter(block -> block instanceof BareSmallLogBlock)
                        .forEach(block -> api.registerChoppableBlockBehavior(block, handler));
            } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            }
        }
    }
}
