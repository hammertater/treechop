package ht.treechop.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class FauxPlayerInteractionManager {

    public static void harvestBlockSkippingOnBlockStartBreak(ServerPlayerEntity player, ServerWorld world, BlockState blockState, BlockPos pos, int exp) {
        TileEntity tileentity = world.getTileEntity(pos);
        Block block = blockState.getBlock();
        if ((block instanceof CommandBlockBlock || block instanceof StructureBlock || block instanceof JigsawBlock) && !player.canUseCommandBlock()) {
            world.notifyBlockUpdate(pos, blockState, blockState, 3);
        } else {
            if (player.getServer() == null || !player.blockActionRestricted(world, pos, player.getServer().getGameType())) {
                if (player.isCreative()) {
                    removeBlock(player, world, pos, false);
                } else {
                    ItemStack itemstack = player.getHeldItemMainhand();
                    ItemStack itemstack1 = itemstack.copy();
                    boolean flag1 = blockState.canHarvestBlock(world, pos, player); // previously player.func_234569_d_(blockstate)
                    itemstack.onBlockDestroyed(world, blockState, pos, player);
                    if (itemstack.isEmpty() && !itemstack1.isEmpty())
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack1, Hand.MAIN_HAND);
                    boolean flag = removeBlock(player, world, pos, flag1);

                    if (flag && flag1) {
                        block.harvestBlock(world, player, pos, blockState, tileentity, itemstack1);
                    }

                    if (flag && exp > 0)
                        blockState.getBlock().dropXpOnBlockBreak(world, pos, exp);
                }
            }
        }
    }

    private static boolean removeBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos, boolean canHarvest) {
        BlockState state = world.getBlockState(pos);
        boolean removed = state.removedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos));
        if (removed)
            state.getBlock().onPlayerDestroy(world, pos, state);
        return removed;
    }

}
