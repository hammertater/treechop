package ht.treechop.common.platform;

import ht.treechop.api.ChopData;
import ht.treechop.api.TreeData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface Platform {

    boolean isDedicatedServer();

    boolean uses(ModLoader loader);

    boolean onStartBlockBreak(Player player, ItemStack tool, BlockPos blockPos);

    TreeData detectTreeEvent(Level level, ServerPlayer agent, BlockPos blockPos, BlockState blockState, boolean overrideLeaves);

    boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData, Object trigger);

    void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData);

    Block getChoppedLogBlock();

    BlockEntityType<?> getChoppedLogBlockEntity();

    boolean doItemDamage(ItemStack tool, Level level, BlockState blockState, BlockPos pos, Player agent);

}
