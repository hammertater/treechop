package ht.treechop.common;

import ht.treechop.api.ChopData;
import ht.treechop.api.ChopEvent;
import ht.treechop.api.TreeData;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.network.ForgePacketHandler;
import ht.treechop.common.platform.Platform;
import ht.treechop.common.registry.ForgeModBlocks;
import ht.treechop.common.settings.EntityChopSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Optional;

public class ForgePlatform implements Platform {

    @Override
    public boolean onStartBlockBreak(Player player, ItemStack tool, BlockPos blockPos) {
        return !tool.getItem().onBlockStartBreak(tool, blockPos, player);
    }

    @Override
    public Optional<EntityChopSettings> getPlayerChopSettings(Player player) {
        return ChopSettingsCapability.forPlayer(player).map(x -> x);
    }

    @Override
    public TreeData detectTreeEvent(Level level, ServerPlayer agent, BlockPos blockPos, BlockState blockState, boolean overrideLeaves) {
        TreeData treeData = new TreeData(overrideLeaves);
        boolean canceled = MinecraftForge.EVENT_BUS.post(new ChopEvent.DetectTreeEvent(level, agent, blockPos, blockState, treeData));
        if (canceled) {
            return TreeData.empty();
        }
        return treeData;
    }

    @Override
    public boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData) {
        ChopEvent.StartChopEvent startChopEvent = new ChopEvent.StartChopEvent(
                level,
                agent,
                pos,
                blockState,
                chopData
        );

        boolean canceled = MinecraftForge.EVENT_BUS.post(startChopEvent);
        return !canceled;
    }

    @Override
    public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData) {
        MinecraftForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(
                level,
                agent,
                pos,
                blockState
        ));
    }

    @Override
    public Block getChoppedLogBlock() {
        return ForgeModBlocks.CHOPPED_LOG.get();
    }

    @Override
    public BlockEntityType<?> getChoppedLogBlockEntity() {
        return ForgeModBlocks.CHOPPED_LOG_ENTITY.get();
    }

    @Override
    public boolean doItemDamage(ItemStack stack, Level level, BlockState blockState, BlockPos pos, Player agent) {
        ItemStack mockItemStack = stack.copy();
        stack.mineBlock(level, blockState, pos, agent);
        if (stack.isEmpty() && !mockItemStack.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(agent, stack, InteractionHand.MAIN_HAND);
            return true;
        } else {
            return false;
        }
    }
}
