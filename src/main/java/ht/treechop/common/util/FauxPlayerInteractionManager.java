package ht.treechop.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;

public class FauxPlayerInteractionManager {

    public static void harvestBlockSkippingOnBlockStartBreak(ServerPlayer player, ServerLevel level, BlockState blockState, BlockPos pos, int exp) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Block block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(pos, blockState, blockState, 3);
        } else if (player.getMainHandItem().onBlockStartBreak(pos, player)) {
        } else if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
        } else {
            if (player.isCreative()) {
                removeBlock(player, level, pos, false);
            } else {
                ItemStack itemstack = player.getMainHandItem();
                ItemStack itemstack1 = itemstack.copy();
                boolean canHarvest = blockState.canHarvestBlock(level, pos, player); // previously player.hasCorrectToolForDrops(blockstate)
                itemstack.mineBlock(level, blockState, pos, player);
                if (itemstack.isEmpty() && !itemstack1.isEmpty())
                    ForgeEventFactory.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
                boolean blockWasRemoved = removeBlock(player, level, pos, canHarvest);

                if (blockWasRemoved && canHarvest) {
                    block.playerDestroy(level, player, pos, blockState, blockEntity, itemstack1);
                }

                if (blockWasRemoved && exp > 0)
                    blockState.getBlock().popExperience(level, pos, exp);
            }
        }
    }

    private static boolean removeBlock(ServerPlayer player, ServerLevel level, BlockPos pos, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed)
            state.getBlock().destroy(level, pos, state);
        return removed;
    }

}
