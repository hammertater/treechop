package ht.treechop.util;

import ht.treechop.TreeChopMod;
import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    private static final ResourceLocation LEAVES_LIKE = new ResourceLocation("treechop", "leaves_like");

    private static final int MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES = 7;
    public static final int FELL_NOISE_INTERVAL = 16;
    public static final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

    static public boolean isBlockChoppable(IWorld world, BlockPos pos, BlockState blockState) {
        return ((blockState.getBlock() instanceof ChoppedLogBlock) ||
                (isBlockALog(blockState) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south()))));// && Arrays.stream(BlockNeighbors.ABOVE).map(pos::add).anyMatch(pos1 -> isBlockALog(world, pos1) || isBlockLeaves(world, pos1)))));
    }

    static public boolean isBlockChoppable(IWorld world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockALog(BlockState blockState) {
        Set<ResourceLocation> tags = blockState.getBlock().getTags();
        return tags.contains(ConfigHandler.blockTagForDetectingLogs);
    }

    static public boolean isBlockALog(IWorld world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(IWorld world, BlockPos pos) {
        return isBlockLeaves(world.getBlockState(pos));
    }

    private static boolean isBlockLeaves(BlockState blockState) {
        Set<ResourceLocation> tags = blockState.getBlock().getTags();
        return tags.contains(ConfigHandler.blockTagForDetectingLeaves);
    }

    static public Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks, AtomicInteger iterationCounter) {
        Set<BlockPos> connectedBlocks = new HashSet<>();
        List<BlockPos> newConnectedBlocks = new LinkedList<>(startingPoints);
        iterationCounter.set(0);
        do {
            connectedBlocks.addAll(newConnectedBlocks);
            if (connectedBlocks.size() >= maxNumBlocks) {
                break;
            }

            newConnectedBlocks = newConnectedBlocks.stream()
                    .flatMap(blockPos -> searchOffsetsSupplier.apply(blockPos)
                            .filter(pos1 -> !connectedBlocks.contains(pos1))
                    )
                    .limit(maxNumBlocks - connectedBlocks.size())
                    .collect(Collectors.toList());

            iterationCounter.incrementAndGet();
        } while (!newConnectedBlocks.isEmpty());

        return connectedBlocks;
    }

    static public Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks) {
        return getConnectedBlocks(startingPoints, searchOffsetsSupplier, maxNumBlocks, new AtomicInteger());
    }

    static public BlockState chipBlock(World world, BlockPos blockPos, int numChops, PlayerEntity agent, ItemStack tool) {
        ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
        BlockState blockState = ModBlocks.CHOPPED_LOG.get().getDefaultState().with(BlockStateProperties.CHOP_COUNT, numChops).with(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
        return harvestAndChangeBlock(world, blockPos, blockState, agent, tool);
    }

    /**
     * @return the new block state, or {@code null} if unable to break the block
     */
    private static BlockState harvestAndChangeBlock(World world, BlockPos blockPos, BlockState newBlockState, PlayerEntity agent, ItemStack tool) {
        if (!tool.onBlockStartBreak(blockPos, agent)) {
            if (!agent.isCreative()) {
                BlockState oldBlockState = world.getBlockState(blockPos);
                TileEntity tileEntity = world.getTileEntity(blockPos);
                Block.spawnDrops(oldBlockState, world, blockPos, tileEntity, agent, tool);
            }
            world.setBlockState(blockPos, newBlockState, 3);
            return newBlockState;
        } else {
            return null;
        }
    }

    public static void fellTree(IWorld world, Collection<BlockPos> treeBlocks, PlayerEntity agent) {
        boolean spawnDrops = !agent.isCreative();

        // Break leaves
        if (ConfigHandler.COMMON.breakLeaves.get()) {
            AtomicInteger iterationCounter = new AtomicInteger();
            Set<BlockPos> leavesToDestroy = new HashSet<>();

            int maxNumLeavesBlocks = ConfigHandler.COMMON.maxNumLeavesBlocks.get();
            getConnectedBlocks(
                    treeBlocks,
                    pos1 -> {
                        Block block = world.getBlockState(pos1).getBlock();
                        return ((block.getTags().contains(LEAVES_LIKE) && !(block instanceof LeavesBlock))
                                    ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                                    : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(world, pos2, iterationCounter, leavesToDestroy));
                    },
                    maxNumLeavesBlocks,
                    iterationCounter
            );

            if (leavesToDestroy.size() >= maxNumLeavesBlocks) {
                TreeChopMod.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d blocks", leavesToDestroy.size(), maxNumLeavesBlocks));
            }

            destroyBlocksWithoutTooMuchNoise(world, leavesToDestroy, spawnDrops);
        }

        destroyBlocksWithoutTooMuchNoise(world, treeBlocks, spawnDrops);
    }

    public static void destroyBlocksWithoutTooMuchNoise(IWorld world, Collection<BlockPos> blocks, boolean spawnDrops) {
        int blockCounter = 0;
        for (BlockPos pos : blocks) {
            if (blockCounter < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter++, FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, spawnDrops);
            } else {
                destroyBlockQuietly(world, pos, spawnDrops);
            }
        }
    }

    public static boolean markLeavesToDestroyAndKeepLooking(IWorld world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        BlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(blockState)) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.get(LeavesBlock.DISTANCE)) {
                    return false;
                }
                else if (blockState.get(LeavesBlock.PERSISTENT)) {
                    return true;
                }
            } else if (blockState.getBlock().getTags().contains(LEAVES_LIKE) && iterationCounter.get() >= MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static void destroyBlockQuietly(IWorld world, BlockPos pos, boolean drop) {
        if (drop) {
            Block.spawnDrops(world.getBlockState(pos), (World) world, pos);
        }
        world.removeBlock(pos, false);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) (ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(numBlocks) * ConfigHandler.COMMON.chopCountScale.get());
    }

}
