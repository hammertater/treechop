package ht.treechop.common.platform;

import ht.treechop.api.*;
import ht.treechop.common.chop.ChopResult;
import ht.treechop.common.chop.FellTreeResult;
import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatform implements Platform {
    @Override
    public boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public boolean uses(ModLoader loader) {
        return loader == ModLoader.FABRIC;
    }

    @Override
    public TreeData detectTreeEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, TreeData treeData) {
        return TreeChopEvents.DETECT_TREE.invoker().onDetectTree(level, player, blockPos, blockState, treeData);
    }

    // Returns true if chopping should continue
    @Override
    public boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData, Object trigger) {
        return TreeChopEvents.BEFORE_CHOP.invoker().beforeChop(
                level,
                agent,
                pos,
                blockState,
                chopData
        );
    }

    @Override
    public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopDataImmutable chopData, ChopResult chopResult) {
        TreeChopEvents.AFTER_CHOP.invoker().afterChop(
                level,
                agent,
                pos,
                blockState,
                chopData,
                chopResult instanceof FellTreeResult
        );
    }

    @Override
    public boolean startFellTreeEvent(ServerPlayer player, Level level, BlockPos choppedPos, FellData fellData) {
        return TreeChopEvents.BEFORE_FELL.invoker().beforeFell(
                level,
                player,
                choppedPos,
                fellData
        );
    }

    @Override
    public Block getChoppedLogBlock() {
        return FabricModBlocks.CHOPPED_LOG;
    }

    @Override
    public BlockEntityType<?> getChoppedLogBlockEntity() {
        return FabricModBlocks.CHOPPED_LOG_ENTITY;
    }

    @Override
    public ResourceLocation getResourceLocationForBlock(Block block) {
        return Registry.BLOCK.getKey(block);
    }

    @Override
    public ResourceLocation getResourceLocationForItem(Item item) {
        return Registry.ITEM.getKey(item);
    }

    @Override
    public BlockState getStrippedState(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return null;
    }
}
