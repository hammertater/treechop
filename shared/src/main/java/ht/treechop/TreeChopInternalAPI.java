package ht.treechop;

import ht.treechop.api.IChoppingItem;
import ht.treechop.api.ITreeChopBlockBehavior;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class TreeChopInternalAPI implements TreeChopAPI {
    private static final Map<Block, ITreeChopBlockBehavior> choppableBlockBehaviors = new HashMap<>();
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
    private static final Map<Item, Boolean> choppingItemOverrides = new HashMap<>();
    private final String modId;

    TreeChopInternalAPI(String modId) {
        this.modId = modId;
    }

    private void print(String message) {
        TreeChop.LOGGER.info(String.format("[API via %s] %s", modId, message));
    }

    @Override
    public void overrideChoppableBlock(Block block, boolean isChoppable) {
        Boolean existingOverride = choppableBlockOverrides.put(block, isChoppable);

        if (existingOverride != null && !existingOverride.equals(isChoppable)) {
            print(String.format("changed \"choppable block\" override for %s from %s to %s",
                    TreeChop.platform.getResourceLocationForBlock(block),
                    existingOverride,
                    isChoppable));
        }
    }

    @Override
    public void overrideLeavesBlock(Block block, boolean isLeaves) {
        Boolean existingOverride = leavesBlockOverrides.put(block, isLeaves);

        if (existingOverride != null && !existingOverride.equals(isLeaves)) {
            print(String.format("changed \"leaves block\" override for %s from %s to %s",
                    TreeChop.platform.getResourceLocationForBlock(block),
                    existingOverride,
                    isLeaves));
        }
    }

    @Override
    public void overrideChoppingItem(Item item, boolean canChop) {
        Boolean existingOverride = choppingItemOverrides.put(item, canChop);

        if (existingOverride != null && !existingOverride.equals(canChop)) {
            print(String.format("changed \"chopping item\" override for %s from %s to %s",
                    TreeChop.platform.getResourceLocationForItem(item),
                    existingOverride,
                    canChop));
        }
    }

    @Override
    public void registerChoppableBlockBehavior(Block block, ITreeChopBlockBehavior handler) {
        ITreeChopBlockBehavior existingHandler = choppableBlockBehaviors.put(block, handler);

        if (existingHandler != null) {
            print(String.format("overrode existing log block behavior for %s",
                    TreeChop.platform.getResourceLocationForBlock(block)));
        } else {
            overrideChoppableBlock(block, true);
        }
    }

    @Override
    public boolean deregisterChoppableBlockBehavior(Block block) {
        return choppableBlockBehaviors.remove(block) == null;
    }

    @Override
    public ITreeChopBlockBehavior getRegisteredChoppableBlockBehavior(Block block) {
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
    public Optional<IChoppingItem> getRegisteredChoppingItemBehavior(Item item) {
        return Optional.ofNullable(choppingItemBehaviors.get(item));
    }
    
    @Override
    public boolean isBlockChoppable(Level level, BlockPos pos, BlockState blockState) {
        return ChopUtil.isBlockALog(level, pos, blockState);
    }

    @Override
    public boolean isBlockLeaves(Level level, BlockPos pos, BlockState blockState) {
        return ChopUtil.isBlockLeaves(blockState);
    }

    @Override
    public boolean canChopWithItem(Player player, ItemStack stack, Level level, BlockPos pos, BlockState blockState) {
        return ChopUtil.canChopWithTool(player, stack, level, pos, blockState);
    }

    public Stream<Pair<Block, Boolean>> getChoppableBlockOverrides() {
        return choppableBlockOverrides.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    public Stream<Pair<Block, Boolean>> getLeavesBlockOverrides() {
        return leavesBlockOverrides.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    public Stream<Pair<Item, Boolean>> getChoppingItemOverrides() {
        return choppingItemOverrides.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }
}
