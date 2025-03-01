package ht.treechop.common;

import ht.treechop.api.*;
import ht.treechop.common.chop.ChopResult;
import ht.treechop.common.chop.FellTreeResult;
import ht.treechop.common.platform.ModLoader;
import ht.treechop.common.platform.Platform;
import ht.treechop.common.util.TreeDataImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgePlatform implements Platform {

    @Override
    public boolean isDedicatedServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    @Override
    public boolean uses(ModLoader loader) {
        return loader == ModLoader.FORGE;
    }

    @Override
    public TreeData detectTreeEvent(Level level, ServerPlayer agent, BlockPos blockPos, BlockState blockState, TreeData treeData) {
        ChopEvent.DetectTreeEvent event = new ChopEvent.DetectTreeEvent(level, agent, blockPos, blockState, treeData);
        NeoForge.EVENT_BUS.post(event);

        treeData = event.getTreeData().orElse(null);
        return (event.isCanceled() || treeData == null) ? TreeDataImpl.empty(level) : treeData;
    }

    @Override
    public boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData, Object trigger) {
        ChopEvent.StartChopEvent startChopEvent = new ChopEvent.StartChopEvent(
                level,
                agent,
                pos,
                blockState,
                chopData,
                trigger
        );

        NeoForge.EVENT_BUS.post(startChopEvent);
        return !startChopEvent.isCanceled();
    }

    @Override
    public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopDataImmutable chopData, ChopResult chopResult) {
        NeoForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(
                level,
                agent,
                pos,
                blockState,
                chopData,
                chopResult instanceof FellTreeResult
        ));
    }

    @Override
    public boolean startFellTreeEvent(ServerPlayer player, Level level, BlockPos choppedPos, FellData fellData) {
        ChopEvent.BeforeFellEvent beforeFellEvent = new ChopEvent.BeforeFellEvent(
                level,
                player,
                choppedPos,
                level.getBlockState(choppedPos),
                fellData
        );
        NeoForge.EVENT_BUS.post(beforeFellEvent);

        return !beforeFellEvent.isCanceled();
    }

    @Override
    public void finishFellTreeEvent(ServerPlayer player, Level level, BlockPos choppedPos, FellData fellData) {
        ChopEvent.AfterFellEvent afterFellEvent = new ChopEvent.AfterFellEvent(
                level,
                player,
                choppedPos,
                level.getBlockState(choppedPos),
                fellData
        );
    }

    @Override
    public Block getChoppedLogBlock() {
        return NeoForgeRegistry.Blocks.CHOPPED_LOG.get();
    }

    @Override
    public BlockEntityType<?> getChoppedLogBlockEntity() {
        return NeoForgeRegistry.BlockEntities.CHOPPED_LOG_ENTITY.get();
    }

    @Override
    public ResourceLocation getResourceLocationForBlock(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    @Override
    public ResourceLocation getResourceLocationForItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    @Override
    public BlockState getStrippedState(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        try {
            if (level instanceof Level realLevel) {
                BlockHitResult fakeHitResult = BlockHitResult.miss(Vec3.ZERO, Direction.DOWN, pos);
                return state.getToolModifiedState(new UseOnContext(realLevel, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, fakeHitResult), ItemAbilities.AXE_STRIP, true);
            }
        } catch (NullPointerException e) {
            // Do nothing
        }
        return null;
    }
}
