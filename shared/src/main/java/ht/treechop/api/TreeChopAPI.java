package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface TreeChopAPI {
    void overrideLogBlock(Block block, boolean isChoppable);

    void overrideLeavesBlock(Block block, boolean isLeaves);

    void overrideChoppingItem(Item item, boolean canChop);

    void registerLogBlockBehavior(Block block, IChoppableBlock handler);

    boolean deregisterLogBlockBehavior(Block block);

    IChoppableBlock getRegisteredLogBlockBehavior(Block block);

    void registerChoppingItemBehavior(Item item, IChoppingItem handler);

    boolean deregisterChoppingItemBehavior(Item item);

    IChoppingItem getRegisteredChoppingItemBehavior(Item block);

    boolean isBlockALog(Level level, BlockPos pos);

    boolean isBlockLeaves(Level level, BlockPos pos);

    boolean isBlockALog(Level level, BlockPos pos, BlockState blockState);

    boolean isBlockLeaves(Level level, BlockPos pos, BlockState blockState);

    boolean isChoppingItem(ItemStack item);
}
