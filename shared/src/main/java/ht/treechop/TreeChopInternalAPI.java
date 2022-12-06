package ht.treechop;

import com.mojang.datafixers.util.Pair;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.api.IChoppingItem;
import ht.treechop.api.ITreeChopBlockBehavior;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class TreeChopInternalAPI implements TreeChopAPI {
    private final String modId;

    private static final Map<Block, IChoppableBlock> choppableBlockBehaviors = new HashMap<>();

    private static final Map<Item, IChoppingItem> choppingItemBehaviors = new HashMap<>();

    private static final Map<Block, Boolean> choppableBlockOverrides = new HashMap<>() {
        @Override
        public Boolean put(Block block, Boolean isChoppable) {
            ConfigHandler.COMMON.choppableBlocks.reset();
            return super.put(block, isChoppable);
        }
    };

    private static final Map<Block, Boolean> leavesBlockOverrides = new HashMap<>() {
        @Override
        public Boolean put(Block block, Boolean isLeaves) {
            ConfigHandler.COMMON.leavesBlocks.reset();
            return super.put(block, isLeaves);
        }
    };

    private static final Map<Item, Boolean> choppingItemOverrides = new HashMap<>() {
        @Override
        public Boolean put(Item item, Boolean canChop) {
            ConfigHandler.COMMON.leavesBlocks.reset();
            return super.put(item, canChop);
        }
    };

    TreeChopInternalAPI(String modId) {
        this.modId = modId;
    }

    private void print(String message) {
        TreeChop.LOGGER.info(String.format("[API via %s] %s", modId, message));
    }

    @Override
    public void overrideLogBlock(Block block, boolean isChoppable) {
        Boolean existingOverride = choppableBlockOverrides.put(block, isChoppable);

        if (existingOverride != null && !existingOverride.equals(isChoppable)) {
            print(String.format("changed \"choppable\" override for block %s from %s to %s",
                    TreeChop.platform.getResourceLocationForBlock(block),
                    existingOverride,
                    isChoppable));
        }
    }

    @Override
    public void overrideLeavesBlock(Block block, boolean isLeaves) {
        Boolean existingOverride = leavesBlockOverrides.put(block, isLeaves);

        if (existingOverride != null && !existingOverride.equals(isLeaves)) {
            print(String.format("changed \"leaves\" override for block %s from %s to %s",
                    TreeChop.platform.getResourceLocationForBlock(block),
                    existingOverride,
                    isLeaves));
        }
    }

    @Override
    public void overrideChoppingItem(Item item, boolean canChop) {
        Boolean existingOverride = choppingItemOverrides.put(item, canChop);

        if (existingOverride != null && !existingOverride.equals(canChop)) {
            print(String.format("changed \"chopping item\" override for item %s from %s to %s",
                    TreeChop.platform.getResourceLocationForItem(item),
                    existingOverride,
                    canChop));
        }
    }

    @Override
    public void registerLogBlockBehavior(Block block, IChoppableBlock handler) {
        ITreeChopBlockBehavior existingHandler = choppableBlockBehaviors.put(block, handler);

        if (existingHandler != null) {
            print(String.format("overrode existing behavior for block %s",
                    TreeChop.platform.getResourceLocationForBlock(block)));
        } else {
            overrideLogBlock(block, true);
        }
    }

    @Override
    public boolean deregisterLogBlockBehavior(Block block) {
        return choppableBlockBehaviors.remove(block) == null;
    }

    @Override
    public IChoppableBlock getRegisteredLogBlockBehavior(Block block) {
        return choppableBlockBehaviors.get(block);
    }

    @Override
    public void registerChoppingItemBehavior(Item item, IChoppingItem handler) {
        IChoppingItem existingHandler = choppingItemBehaviors.put(item, handler);

        if (existingHandler != null) {
            print(String.format("overrode existing behavior for item %s",
                    TreeChop.platform.getResourceLocationForItem(item)));
        } else {
            overrideChoppingItem(item, true);
        }
    }

    @Override
    public boolean deregisterChoppingItemBehavior(Item item) {
        return choppingItemBehaviors.remove(item) == null;
    }

    @Override
    public IChoppingItem getRegisteredChoppingItemBehavior(Item item) {
        return choppingItemBehaviors.get(item);
    }

    @Override
    public boolean isBlockALog(Level level, BlockPos pos) {
        return isBlockALog(level, pos, level.getBlockState(pos));
    }

    @Override
    public boolean isBlockALog(Level level, BlockPos pos, BlockState blockState) {
        return ChopUtil.isBlockALog(level, pos, blockState);
    }

    @Override
    public boolean isBlockLeaves(Level level, BlockPos pos) {
        return isBlockLeaves(level, pos, level.getBlockState(pos));
    }

    @Override
    public boolean isBlockLeaves(Level level, BlockPos pos, BlockState blockState) {
        return ChopUtil.isBlockLeaves(blockState);
    }

    @Override
    public boolean isChoppingItem(ItemStack stack) {
        return ChopUtil.canChopWithTool(stack);
    }

    public Stream<Pair<Block, Boolean>> getChoppableBlockOverrides() {
        return choppableBlockOverrides.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    public Stream<Pair<Block, Boolean>> getLeavesBlockOverrides() {
        return leavesBlockOverrides.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }
}
