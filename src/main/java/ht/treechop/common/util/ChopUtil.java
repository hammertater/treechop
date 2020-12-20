package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.IChoppable;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    private static final Random RANDOM = new Random();

    public static final int FELL_NOISE_INTERVAL = 16;
    public static final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

    static public boolean isBlockChoppable(World world, BlockPos pos, IBlockState blockState) {
        return ((blockState.getBlock() instanceof IChoppable) ||
                (isBlockALog(world, pos, blockState) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south()))));// && Arrays.stream(BlockNeighbors.ABOVE).map(pos::add).anyMatch(pos1 -> isBlockALog(world, pos1) || isBlockLeaves(world, pos1)))));
    }

    static public boolean isBlockChoppable(World world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockALog(World world, BlockPos pos, IBlockState blockState) {
        Block block = blockState.getBlock();
        return (block instanceof IChoppable
                || ConfigHandler.getLogBlocks().contains(block)
                || ConfigHandler.getLogItems().contains(block.getPickBlock(blockState, null, world, pos, null).getItem())
                || isMushroomStem(blockState)
        );
    }

    static public boolean isBlockALog(World world, BlockPos pos) {
        return isBlockALog(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(World world, BlockPos pos) {
        return isBlockLeaves(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(World world, BlockPos pos, IBlockState blockState) {
        Block block = blockState.getBlock();
        return (ConfigHandler.getLeavesBlocks().contains(block)
                || ConfigHandler.getLeavesItems().contains(block.getPickBlock(blockState, null, world, pos, null).getItem())
                || isMushroomCap(blockState)
        );
    }

    private static boolean isMushroomCap(IBlockState blockState) {
        if (blockState.getBlock() instanceof BlockHugeMushroom) {
            BlockHugeMushroom.EnumType variant = blockState.getValue(BlockHugeMushroom.VARIANT);
            return variant != BlockHugeMushroom.EnumType.STEM && variant != BlockHugeMushroom.EnumType.ALL_STEM;
        }
        return false;
    }

    private static boolean isMushroomStem(IBlockState blockState) {
        if (blockState.getBlock() instanceof BlockHugeMushroom) {
            BlockHugeMushroom.EnumType variant = blockState.getValue(BlockHugeMushroom.VARIANT);
            return variant == BlockHugeMushroom.EnumType.STEM || variant == BlockHugeMushroom.EnumType.ALL_STEM;
        }
        return false;
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

    /**
     * If the block at {@code blockPos} is chippable, harvest it and replace with a chopped block.
     * @return the new {@code IBlockState} after chipping or {@code null} if the existing block cannot be chipped
     */
    static public IBlockState chipBlock(World world, BlockPos blockPos, int numChops, EntityPlayer agent, ItemStack tool) {
        ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
        IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IChoppable) {
            throw new IllegalArgumentException("Block is already chipped");
        }

        Block choppedBlock = (Block) getChoppedBlock(world, blockPos, blockState, shape);
        if (choppedBlock != null) {
            IBlockState choppedState = choppedBlock.getDefaultState()
                    .withProperty(BlockStateProperties.CHOP_COUNT, numChops);
            return tryToChangeBlock(world, blockPos, choppedState, agent, tool);
        } else {
            return null;
        }
    }

    /**
     * @return the new block state, or {@code null} if unable to break the block
     */
    private static IBlockState tryToChangeBlock(World world, BlockPos blockPos, IBlockState newBlockState, EntityPlayer agent, ItemStack tool) {
        if (tool.isEmpty() && tool.getItem().onBlockStartBreak(tool, blockPos, agent)) {
            return null;
        } else {
            world.setBlockState(blockPos, newBlockState, 3);
            return newBlockState;
        }
    }

    public static void fellTree(World world, Collection<BlockPos> treeBlocks, EntityPlayer agent) {
        boolean spawnDrops = !agent.isCreative();

        // Break leaves
        if (ConfigHandler.breakLeaves) {
            AtomicInteger iterationCounter = new AtomicInteger();
            Set<BlockPos> leavesToDestroy = new HashSet<>();

            int maxNumLeavesBlocks = ConfigHandler.maxNumLeavesBlocks;
            getConnectedBlocks(
                    treeBlocks,
                    pos1 -> {
                        IBlockState blockState = world.getBlockState(pos1);
                        Block block = blockState.getBlock();
                        return ((isBlockLeaves(world, pos1, blockState) && !(block instanceof BlockLeaves))
                                    ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                                    : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(world, pos2, iterationCounter, leavesToDestroy));
                    },
                    maxNumLeavesBlocks,
                    iterationCounter
            );

            if (leavesToDestroy.size() >= maxNumLeavesBlocks) {
                TreeChopMod.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d block", leavesToDestroy.size(), maxNumLeavesBlocks));
            }

            destroyBlocksWithoutTooMuchNoise(world, leavesToDestroy, spawnDrops);
        }

        destroyBlocksWithoutTooMuchNoise(world, treeBlocks, spawnDrops);
    }

    public static void destroyBlocksWithoutTooMuchNoise(World world, Collection<BlockPos> blocks, boolean spawnDrops) {
        int blockCounter = 0;
        for (BlockPos pos : blocks) {
            if (blockCounter < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter++, FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, spawnDrops);
            } else {
                destroyBlockQuietly(world, pos, spawnDrops);
            }
        }
    }

    public static boolean markLeavesToDestroyAndKeepLooking(World world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        IBlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(world, pos, blockState)) {
            if (blockState.getBlock() instanceof BlockLeaves) {
                if (iterationCounter.get() + 1 > ConfigHandler.maxBreakLeavesDistance) {
                    return false;
                }
                else if (!blockState.getValue(BlockLeaves.DECAYABLE)) {
                    return true;
                }
            } else if (iterationCounter.get() >= ConfigHandler.maxBreakLeavesDistance) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static void destroyBlockQuietly(World world, BlockPos pos, boolean drop) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (drop) {
            block.dropBlockAsItem(world, pos, blockState, 0);
        }
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) (ConfigHandler.chopCountingAlgorithm.calculate(numBlocks) * ConfigHandler.chopCountScale);
    }

    public static BlockThatWasBroken chop(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool, boolean fellIfPossible) {
        return fellIfPossible
                ? chop(world, blockPos, agent, numChops, tool)
                : chopNoFell(world, blockPos, agent, numChops, tool);
    }

    public static BlockThatWasBroken chop(World world, final BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
        if (!isBlockChoppable(world, blockPos, blockState)) {
            return BlockThatWasBroken.IGNORED;
        }

        int maxNumTreeBlocks = ConfigHandler.maxNumTreeBlocks;

        AtomicBoolean hasLeaves = new AtomicBoolean(!getPlayerChopSettings(agent).getOnlyChopTreesWithLeaves());
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> hasLeaves.compareAndSet(false, isBlockLeaves(world, pos)))
                        .filter(pos -> isBlockALog(world, pos)),
                maxNumTreeBlocks
        );

        if (!hasLeaves.get()) {
            return BlockThatWasBroken.IGNORED;
        }

        if (supportedBlocks.size() >= maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d block (not including leaves)", supportedBlocks.size(), maxNumTreeBlocks));
        }

        int numChopsToFell = numChopsToFell(supportedBlocks.size());
        int currentNumChops = getNumChops(blockState);

        if (currentNumChops + numChops >= numChopsToFell) {
            BlockThatWasBroken result = new BlockThatWasBroken(world, blockPos, blockState, agent);
            fellTree(world, supportedBlocks, agent);
            return result;
        } else {
            Set<BlockPos> nearbyChoppableBlocks;
            nearbyChoppableBlocks = ChopUtil.getConnectedBlocks(
                    Collections.singletonList(blockPos),
                    pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos)
                            .filter(checkPos -> Math.abs(checkPos.getY() - blockPos.getY()) < 4 && isBlockChoppable(world, checkPos)),
                    64
            );

            int totalNumChops = nearbyChoppableBlocks.stream()
                    .map(world::getBlockState)
                    .map(blockState1 -> blockState1.getBlock() instanceof IChoppable
                            ? ((IChoppable) blockState1.getBlock()).getNumChops(blockState1)
                            : 0
                    )
                    .reduce(Integer::sum)
                    .orElse(0)
                    + numChops; // Include this chop

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof IChoppable)
                        .sorted(Comparator.comparingInt(BlockPos::getY))
                        .collect(Collectors.toList());

                for (BlockPos pos : choppedLogsSortedByY) {
                    int chops = getNumChops(world, pos);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }

                BlockThatWasBroken result = new BlockThatWasBroken(world, blockPos, blockState, agent);
                fellTree(world, supportedBlocks, agent);
                return result;
            } else {
                int newNumChops;
                if ((newNumChops = currentNumChops + numChops) <= getMaxNumChops(world, blockPos, blockState)) {
                    BlockThatWasBroken result = new BlockThatWasBroken(world, blockPos, blockState, agent);
                    setNumChops(world, blockPos, newNumChops, agent, tool);
                    return result;
                } else { // If this block is out of chops, chop another block
                    int chopsToTargetedBlock = getMaxNumChops(world, blockPos, blockState) - currentNumChops;
                    if (chopsToTargetedBlock > 0) {
                        setNumChops(world, blockPos, getMaxNumChops(world, blockPos, blockState), agent, tool);
                        numChops -= chopsToTargetedBlock;
                    }

                    List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                            .filter(blockPos1 -> {
                                IBlockState blockState1 = world.getBlockState(blockPos1);
                                Block block1 = blockState1.getBlock();
                                if (block1 instanceof IChoppable) {
                                    return ((IChoppable) block1).getNumChops(blockState1) < 7;
                                } else {
                                    return blockPos1.getY() >= blockPos.getY();
                                }
                            })
                            .sorted(Comparator.comparingInt(a -> chopDistance(blockPos, a)))
                            .collect(Collectors.toList());

                    if (!sortedChoppableBlocks.isEmpty()) {
                        // Find a close, choppable block...
                        int choiceIndexLimit = 1;
                        for (int maxChoiceDistance = chopDistance(blockPos, sortedChoppableBlocks.get(0)), n = sortedChoppableBlocks.size(); choiceIndexLimit < n; ++choiceIndexLimit) {
                            if (chopDistance(blockPos, sortedChoppableBlocks.get(choiceIndexLimit)) > maxChoiceDistance) {
                                break;
                            }
                        }

                        // ...and chop it
                        BlockPos choppedPos = sortedChoppableBlocks.get(Math.floorMod(RANDOM.nextInt(), choiceIndexLimit));
                        IBlockState choppedState = world.getBlockState(choppedPos);
                        BlockThatWasBroken result = new BlockThatWasBroken(world, choppedPos, choppedState, agent);
                        if (choppedState.getBlock() instanceof IChoppable) {
                            IChoppable choppedBlock = (IChoppable) choppedState.getBlock();
                            newNumChops = choppedBlock.getNumChops(choppedState) + numChops;
                            world.setBlockState(choppedPos, choppedBlock.withChops(choppedState, newNumChops), 3);
                        } else {
                            IBlockState chippedState = chipBlock(world, choppedPos, numChops, agent, tool);
                            if (chippedState == null) {
                                return BlockThatWasBroken.IGNORED;
                            }
                        }
                        return result;
                    } else {
                        return BlockThatWasBroken.IGNORED;
                    }
                }
            }
        }
    }

    private static IBlockState setNumChops(World world, BlockPos blockPos, int newNumChops, EntityPlayer agent, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            if (newNumChops <= ((IChoppable) block).getMaxNumChops()) {
                IBlockState newBlockState = ((IChoppable) block).withChops(blockState, newNumChops);
                if (world.setBlockState(blockPos, newBlockState)) {
                    return newBlockState;
                } else {
                    throw new IllegalArgumentException("Failed to chop block located at " + blockPos.toString());
                }
            } else {
                throw new IllegalArgumentException("Can't set number of chops above the maximum allowed");
            }
        } else {
            return chipBlock(world, blockPos, newNumChops, agent, tool);
        }
    }

    private static int getMaxNumChops(World world, BlockPos pos, IBlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            return ((IChoppable) block).getMaxNumChops();
        } else {
            IChoppable choppedBlock = getChoppedBlock(world, pos, blockState, ChoppedLogShape.PILLAR);
            return (choppedBlock != null) ? choppedBlock.getMaxNumChops() : 0;
        }
    }

    private static IChoppable getChoppedBlock(World world, BlockPos pos, IBlockState blockState, ChoppedLogShape shape) {
        if (isBlockALog(world, pos, blockState)) {
            return (blockState.getBlock() instanceof IChoppable)
                    ? (IChoppable) blockState.getBlock()
                    : ModBlocks.CHOPPED_LOGS.get(shape);
        } else {
            return null;
        }
    }

    public static int getNumChops(World world, BlockPos pos) {
        return getNumChops(world.getBlockState(pos));
    }

    public static int getNumChops(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? ((IChoppable) block).getNumChops(blockState) : 0;
    }

    private static BlockThatWasBroken chopNoFell(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IChoppable) {
            IChoppable block = (IChoppable) blockState.getBlock();
            int newNumChops = block.getNumChops(blockState) + numChops;
            if (newNumChops <= block.getMaxNumChops()) {
                BlockThatWasBroken result = new BlockThatWasBroken(world, blockPos, blockState, agent);
                world.setBlockState(blockPos, block.withChops(blockState, newNumChops));
                return result;
            } else {
                return BlockThatWasBroken.IGNORED;
            }
        } else {
            BlockThatWasBroken result = new BlockThatWasBroken(world, blockPos, blockState, agent);
            IBlockState chippedState = chipBlock(world, blockPos, numChops, agent, tool);
            return chippedState == null ? BlockThatWasBroken.IGNORED : result;
        }
    }

    // Copied from 1.16.4 Vector3i::manhattanDistance
    public static int manhattanDistance(BlockPos a, BlockPos b) {
        float f = (float)Math.abs(a.getX() - b.getX());
        float f1 = (float)Math.abs(a.getY() - b.getY());
        float f2 = (float)Math.abs(a.getZ() - b.getZ());
        return (int)(f + f1 + f2);
    }

    public static int chopDistance(BlockPos a, BlockPos b) {
        return manhattanDistance(a, b);
    }

    public static boolean canChopWithTool(ItemStack tool) {
        return !ConfigHandler.getChoppingToolBlacklistItems().contains(tool.getItem());
    }

    public static int getNumChopsByTool(ItemStack tool) {
        return 1;
    }

    public static boolean playerWantsToChop(EntityPlayer player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        if (ConfigHandler.canChooseNotToChop) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return true;
        }
    }

    public static boolean playerWantsToFell(EntityPlayer player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    private static boolean isLocalPlayer(EntityPlayer player) {
        return !player.isServerWorld() && Minecraft.getMinecraft().player == player;
    }

    @SuppressWarnings("ConstantConditions")
    private static ChopSettings getPlayerChopSettings(EntityPlayer player) {
        return isLocalPlayer(player) ? Client.getChopSettings() : player.getCapability(ChopSettingsCapability.CAPABILITY, null);
    }

    public static void doItemDamage(ItemStack itemStack, World world, IBlockState blockState, BlockPos blockPos, EntityPlayer agent) {
        ItemStack mockItemStack = itemStack.copy();
        itemStack.onBlockDestroyed(world, blockState, blockPos, agent);
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, EnumHand.MAIN_HAND);
        }
    }

    public static void dropExperience(World world, BlockPos blockPos, IBlockState blockState, int amount) {
        if (world instanceof WorldServer) {
            blockState.getBlock().dropXpOnBlockBreak(world, blockPos, amount);
        }
    }

    public static void harvestBlock(World world, BlockPos blockPos, EntityPlayer agent, ItemStack tool, TileEntity tileEntity, IBlockState blockState) {
            blockState.getBlock().harvestBlock(world, agent, blockPos, blockState, tileEntity, tool); // handles exhaustion, stat-keeping, enchantment effects, and item spawns
    }
}
