package ht.treechop.common.util;

import ht.treechop.TreeChop;
import ht.treechop.api.*;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ClassUtil {
    @Nullable
    public static IChoppableBlock getChoppableBlock(BlockGetter level, BlockPos blockPos, BlockState blockState) {
        IChoppableBlock choppableBlock = getChoppableBlockUnchecked(blockState.getBlock());
        return (choppableBlock != null && choppableBlock.isChoppable(level, blockPos, blockState))
                ? choppableBlock
                : null;
    }

    @Nullable
    public static IChoppableBlock getChoppableBlockUnchecked(Block block) {
        if (block instanceof IChoppableBlock choppableBlock) {
            return choppableBlock;
        } else if (TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IChoppableBlock choppableBlock) {
            return choppableBlock;
        } else if (ConfigHandler.COMMON.choppableBlocks.get().contains(block)) {
            return (IChoppableBlock) TreeChop.platform.getChoppedLogBlock();
        } else {
            return null;
        }
    }

    @Nullable
    public static IFellableBlock getFellableBlock(Block block) {
        if (block instanceof IFellableBlock fellableBlock) {
            return fellableBlock;
        } else if (TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IFellableBlock fellableBlock) {
            return fellableBlock;
        } else {
            return null;
        }
    }

    @Nullable
    public static ICylinderBlock getCylinderBlock(Block block) {
        if (block instanceof ICylinderBlock cylinderBlock) {
            return cylinderBlock;
        } else if (TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof ICylinderBlock cylinderBlock) {
            return cylinderBlock;
        } else {
            return null;
        }
    }

    public static IStrippableBlock getStrippableBlock(Block block) {
        if (block instanceof IStrippableBlock strippableBlock) {
            return strippableBlock;
        } else if (TreeChop.api.getRegisteredChoppableBlockBehavior(block) instanceof IStrippableBlock strippableBlock) {
            return strippableBlock;
        } else {
            return null;
        }
    }

    public static IChoppingItem getChoppingItem(Item item) {
        if (item instanceof IChoppingItem choppingItem) {
            return choppingItem;
        } else {
            return TreeChop.api.getRegisteredChoppingItemBehavior(item);
        }
    }
}
