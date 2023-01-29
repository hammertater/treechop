package ht.treechop;

import ht.treechop.api.IChoppingItem;
import ht.treechop.api.ITreeChopBlockBehavior;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class TreeChopInternalAPI implements TreeChopAPI {
    private static final Map<Block, ITreeChopBlockBehavior> choppableBlockBehaviors = new HashMap<>();
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

    private static final Map<Item, IChoppingItem> choppingItemBehaviors = new HashMap<>();
    private static final Map<Item, Boolean> choppingItemOverrides = new HashMap<>();

    private static final Marker API_MARKER = MarkerManager.getMarker("API");

    private final String modId;

    TreeChopInternalAPI(String modId) {
        this.modId = modId;
    }

    private void print(String message) {
        if (ConfigHandler.COMMON.verboseAPI.get()) {
            TreeChop.LOGGER.info(API_MARKER, "API [{}] {}", modId, message);
        }
    }

    @Override
    public void overrideChoppableBlock(Block block, boolean isChoppable) {
        choppableBlockOverrides.put(block, isChoppable);
        print(String.format("set isChoppable=%s for block %s",
                isChoppable,
                TreeChop.platform.getResourceLocationForBlock(block)));
    }

    @Override
    public void overrideLeavesBlock(Block block, boolean isLeaves) {
        leavesBlockOverrides.put(block, isLeaves);
        print(String.format("set isLeaves=%s for block %s",
                isLeaves,
                TreeChop.platform.getResourceLocationForBlock(block)));
    }

    @Override
    public void overrideChoppingItem(Item item, boolean canChop) {
        choppingItemOverrides.put(item, canChop);
        print(String.format("set canChop=%s for item %s",
                canChop,
                TreeChop.platform.getResourceLocationForItem(item)));
    }

    @Override
    public void registerChoppableBlockBehavior(Block block, ITreeChopBlockBehavior handler) {
        choppableBlockBehaviors.put(block, handler);
        print(String.format("registered new behavior for block %s",
                TreeChop.platform.getResourceLocationForBlock(block)));
    }

    @Override
    public boolean deregisterChoppableBlockBehavior(Block block) {
        ITreeChopBlockBehavior existing = choppableBlockBehaviors.remove(block);
        print(String.format("deregistered behavior for block %s",
                TreeChop.platform.getResourceLocationForBlock(block)));
        return existing == null;
    }

    @Override
    public ITreeChopBlockBehavior getRegisteredChoppableBlockBehavior(Block block) {
        return choppableBlockBehaviors.get(block);
    }

    @Override
    public void registerChoppingItemBehavior(Item item, IChoppingItem handler) {
        choppingItemBehaviors.put(item, handler);
        print(String.format("registered new behavior for item %s",
                TreeChop.platform.getResourceLocationForItem(item)));
    }

    @Override
    public boolean deregisterChoppingItemBehavior(Item item) {
        IChoppingItem existing = choppingItemBehaviors.remove(item);
        print(String.format("deregistered behavior for item %s",
                TreeChop.platform.getResourceLocationForItem(item)));
        return existing == null;
    }

    @Override
    public IChoppingItem getRegisteredChoppingItemBehavior(Item item) {
        return choppingItemBehaviors.get(item);
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
