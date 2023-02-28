package ht.treechop.common;

import ht.treechop.api.ChopData;
import ht.treechop.api.ChopDataImmutable;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.platform.ModLoader;
import ht.treechop.common.platform.Platform;
import ht.treechop.common.registry.ForgeModBlocks;
import ht.treechop.common.util.TreeDataImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePlatform implements Platform {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean isDedicatedServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    @Override
    public boolean uses(ModLoader loader) {
        return loader == ModLoader.FORGE;
    }

    @Override
    public TreeDataImpl detectTreeEvent(Level level, ServerPlayer agent, BlockPos blockPos, BlockState blockState, boolean overrideLeaves) {
        TreeDataImpl treeData = new TreeDataImpl(overrideLeaves);
        boolean canceled = MinecraftForge.EVENT_BUS.post(new ChopEvent.DetectTreeEvent(level, agent, blockPos, blockState, treeData));
        if (canceled) {
            return TreeDataImpl.empty();
        }
        return treeData;
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

        boolean canceled = MinecraftForge.EVENT_BUS.post(startChopEvent);
        return !canceled;
    }

    @Override
    public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopDataImmutable chopData, boolean felled) {
        MinecraftForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(
                level,
                agent,
                pos,
                blockState,
                chopData,
                felled));
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
    public ResourceLocation getResourceLocationForBlock(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    @Override
    public ResourceLocation getResourceLocationForItem(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    @Override
    public BlockState getStrippedState(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        try {
            if (level instanceof Level realLevel) {
                BlockHitResult fakeHitResult = BlockHitResult.miss(Vec3.ZERO, Direction.DOWN, pos);
                return state.getToolModifiedState(new UseOnContext(realLevel, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, fakeHitResult), ToolActions.AXE_STRIP, true);
            }
        } catch (NullPointerException e) {
            // Do nothing
        }
        return null;
    }
}
