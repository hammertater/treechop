package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface TreeChopAPI {
    /**
     * Changes whether the specified block can be chopped. Overrides the TreeChop configuration files. See {@link
     * #isBlockChoppable}
     *
     * @param isChoppable set to {@code true} to mark {@code block} as choppable
     */
    void overrideChoppableBlock(Block block, boolean isChoppable);

    /**
     * Changes whether the specified block is considered leaves. Overrides the TreeChop configuration files. See {@link
     * #isBlockLeaves}
     *
     * @param isLeaves set to {@code true} to mark {@code block} as leaves
     */
    void overrideLeavesBlock(Block block, boolean isLeaves);

    /**
     * Changes whether chopping is allowed for players holding the specified item in their main hand.
     *
     * @param canChop set to {@code true} to allow chopping for {@code item}
     */
    void overrideChoppingItem(Item item, boolean canChop);

    /**
     * Changes chop-related behaviors for a specified block. See {@link ISimpleChoppableBlock} for a default handler
     * implementation. For more advanced control, see {@link IChoppableBlock}, {@link IFellableBlock}, {@link
     * IStrippableBlock}, and {@link ICylinderBlock}.
     */
    void registerChoppableBlockBehavior(Block block, ITreeChopBlockBehavior handler);

    /**
     * Removes a registered block handler.
     */
    boolean deregisterChoppableBlockBehavior(Block block);

    /**
     * Retrieves a registered block handler.
     *
     * @return {@code null} if no handler is registered for {@code block}
     */
    ITreeChopBlockBehavior getRegisteredChoppableBlockBehavior(Block block);

    /**
     * Changes chopping behaviors while holding a specified item.
     */
    void registerChoppingItemBehavior(Item item, IChoppingItem handler);

    /**
     * Removes a registered item handler.
     */
    boolean deregisterChoppingItemBehavior(Item item);

    /**
     * Retrieves a registered item handler.
     *
     * @return {@code null} if no handler is registered for {@code item}
     */
    Optional<IChoppingItem> getRegisteredChoppingItemBehavior(Item item);

    /**
     * Using default config settings, block states with the {@link LeavesBlock#PERSISTENT} property set to {@code true}
     * are not considered leaves, and connected leaves are automatically broken when a tree is felled.
     *
     * @return {@code true} if TreeChop treats the block as leaves.
     */
    boolean isBlockLeaves(Level level, BlockPos pos, BlockState blockState);

    /**
     * Using default config settings, all logs and mushroom stems should be choppable, as well as any blocks that
     * implement {@link IChoppableBlock}, have a registered handler (see {@link #registerChoppableBlockBehavior}), or
     * have been made choppable using {@link #overrideChoppableBlock}.
     * <p>
     * Even if a block is choppable, a player may still choose not to chop it if, for example, it does not have
     * connected leaves.
     *
     * @return {@code true} if the block can be chopped.
     */
    boolean isBlockChoppable(Level level, BlockPos pos, BlockState blockState);

    /**
     * Items can be blacklisted or whitelisted from chopping using the mod configuration files. Alternatively, items can
     * implement {@link IChoppingItem} for more advanced control over chopping.
     *
     * @return true {@code true} if the player may chop the block by breaking it with the item.
     */
    boolean canChopWithItem(Player player, ItemStack stack, Level level, BlockPos pos, BlockState blockState);
}
