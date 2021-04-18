package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.IChoppable;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
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
import java.util.function.Predicate;
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
        return block instanceof IChoppable || ConfigHandler.getLogBlocks().contains(block) || isMushroomStem(blockState);
    }

    static public boolean isBlockALog(World world, BlockPos pos) {
        return isBlockALog(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(World world, BlockPos pos) {
        return isBlockLeaves(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(World world, BlockPos pos, IBlockState blockState) {
        Block block = blockState.getBlock();
        return ConfigHandler.getLeavesBlocks().contains(block) || isMushroomCap(blockState);
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


    @SuppressWarnings("ConstantConditions")
    public static boolean canChangeBlock(BlockPos blockPos, EntityPlayer agent, ItemStack tool) {
        return !(!tool.isEmpty() && tool.getItem().onBlockStartBreak(tool, blockPos, agent));
    }

    public static List<BlockPos> getTreeLeaves(World world, Collection<BlockPos> treeBlocks) {
        AtomicInteger iterationCounter = new AtomicInteger();
        Set<BlockPos> leaves = new HashSet<>();

        int maxNumLeavesBlocks = ConfigHandler.maxNumLeavesBlocks;
        getConnectedBlocks(
                treeBlocks,
                pos1 -> {
                    IBlockState blockState = world.getBlockState(pos1);
                    return ((isBlockLeaves(world, pos1, blockState) && !(blockState.getBlock() instanceof BlockLeaves))
                            ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                            : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(world, pos2, iterationCounter, leaves));
                },
                maxNumLeavesBlocks,
                iterationCounter
        );

        if (leaves.size() >= maxNumLeavesBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d blocks", leaves.size(), maxNumLeavesBlocks));
        }

        return new ArrayList<>(leaves);
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

    static public int numChopsToFell(int numBlocks) {
        return ConfigHandler.chopCountingAlgorithm.calculate(numBlocks);
    }

    public static ChopResult getChopResult(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool, boolean fellIfPossible, Predicate<BlockPos> logCondition) {
        return fellIfPossible
                ? getChopResult(world, blockPos, agent, numChops, tool, logCondition)
                : tryToChopWithoutFelling(world, blockPos, agent, numChops, tool);
    }

    public static ChopResult getChopResult(World world, final BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool, Predicate<BlockPos> logCondition) {
        if (!isBlockChoppable(world, blockPos,  world.getBlockState(blockPos))) {
            return ChopResult.IGNORED;
        }

        int maxNumTreeBlocks = ConfigHandler.maxNumTreeBlocks;

        AtomicBoolean hasLeaves = new AtomicBoolean(!getPlayerChopSettings(agent).getTreeMustHaveLeaves());
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> hasLeaves.compareAndSet(false, isBlockLeaves(world, pos)))
                        .filter(logCondition),
                maxNumTreeBlocks
        );

        if (!hasLeaves.get()) {
            return ChopResult.IGNORED;
        }

        if (supportedBlocks.size() >= maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d block (not including leaves)", supportedBlocks.size(), maxNumTreeBlocks));
        }

        return chopTree(world, blockPos, supportedBlocks, numChops);
    }

    public static ChopResult chopTree(World world, BlockPos target, Set<BlockPos> supportedBlocks, int numChops) {
        IBlockState blockState = world.getBlockState(target);
        int currentNumChops = getNumChops(blockState);
        int numChopsToFell = numChopsToFell(supportedBlocks.size());

        if (currentNumChops + numChops < numChopsToFell) {
            Set<BlockPos> nearbyChoppableBlocks;
            nearbyChoppableBlocks = ChopUtil.getConnectedBlocks(
                    Collections.singletonList(target),
                    pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos)
                            .filter(checkPos -> Math.abs(checkPos.getY() - target.getY()) < 4 && isBlockChoppable(world, checkPos)),
                    64
            );

            int totalNumChops = getNumChops(world, nearbyChoppableBlocks) + numChops;

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof IChoppable)
                        .sorted(Comparator.comparingInt(BlockPos::getY))
                        .collect(Collectors.toList());

                // Consume nearby chopped blocks that contributed even if they're at a lower Y, but prefer higher ones
                for (BlockPos pos : choppedLogsSortedByY) {
                    int chops = getNumChops(world, pos);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }

            } else {
                return gatherChops(world, target, numChops, nearbyChoppableBlocks);
            }
        }

        supportedBlocks.remove(target);
        return new ChopResult(world, Collections.singletonList(target), supportedBlocks);
    }

    /**
     * Adds chops to the targeted block without destroying it. Overflow chops spill to nearby blocks.
     * @param nearbyChoppableBlocks must not include {@code target}
     */
    public static ChopResult gatherChops(World world, BlockPos target, int numChops, Set<BlockPos> nearbyChoppableBlocks) {
        List<TreeBlock> choppedBlocks = new LinkedList<>();
        int numChopsLeft = gatherChopAndGetNumChopsRemaining(world, target, numChops, choppedBlocks);

        if (numChopsLeft > 0) {
            List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                    .filter(blockPos1 -> {
                        IBlockState blockState1 = world.getBlockState(blockPos1);
                        Block block1 = blockState1.getBlock();
                        if (block1 instanceof IChoppable) {
                            return getNumChops(blockState1) < getMaxNumChops(world, target, blockState1);
                        } else {
                            return blockPos1.getY() >= target.getY();
                        }
                    })
                    .sorted(Comparator.comparingInt(a -> chopDistance(target, a)))
                    .collect(Collectors.toList());

            if (sortedChoppableBlocks.size() > 0) {
                int nextChoiceDistance = chopDistance(target, sortedChoppableBlocks.get(0));
                int candidateStartIndex = 0;
                for (int i = 0, n = sortedChoppableBlocks.size(); i <= n; ++i) {
                    if (i == n || chopDistance(target, sortedChoppableBlocks.get(i)) > nextChoiceDistance) {
                        List<BlockPos> candidates = sortedChoppableBlocks.subList(candidateStartIndex, i);
                        Collections.shuffle(candidates);

                        for (BlockPos nextTarget : candidates) {
                            numChopsLeft = gatherChopAndGetNumChopsRemaining(world, nextTarget, numChopsLeft, choppedBlocks);
                            if (numChopsLeft <= 0) {
                                break;
                            }
                        }

                        if (numChopsLeft <= 0) {
                            break;
                        }
                        candidateStartIndex = i;
                    }
                }

            }
        }

        return new ChopResult(choppedBlocks);
    }

    private static int gatherChopAndGetNumChopsRemaining(World world, BlockPos target, int numChops, List<TreeBlock> choppedBlocks) {
        IBlockState blockStateBeforeChopping = world.getBlockState(target);
        IBlockState blockStateAfterChopping = getBlockStateAfterChops(world, target, numChops, false);

        if (blockStateBeforeChopping != blockStateAfterChopping) {
            choppedBlocks.add(new TreeBlock(world, target, blockStateAfterChopping));
        }

        return numChops - (getNumChops(blockStateAfterChopping) - getNumChops(blockStateBeforeChopping));
    }

    private static IBlockState getBlockStateAfterChops(World world, BlockPos blockPos, int numChops, boolean destructive) {
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            return getBlockStateAfterChops(world, blockPos, blockState, numChops, destructive);
        } else {
            ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
            IChoppable choppedBlock = getChoppedBlock(world, blockPos, blockState, shape);
            if (choppedBlock instanceof Block) {
                IBlockState defaultChoppedState = ((Block) choppedBlock).getDefaultState();
                return getBlockStateAfterChops(
                        world,
                        blockPos,
                        defaultChoppedState,
                        numChops - getNumChops(defaultChoppedState),
                        destructive
                );
            } else {
                throw new IllegalArgumentException(String.format("Block \"%s\" is not choppable", block.getRegistryName()));
            }
        }
    }

    private static IBlockState getBlockStateAfterChops(World world, BlockPos pos, IBlockState blockState, int numChops, boolean destructive) {
        Block block = blockState.getBlock();
        int currentNumChops = getNumChops(blockState);
        int maxNumChops = getMaxNumChops(world, pos, blockState);
        int newNumChops = currentNumChops + numChops;

        if (newNumChops <= maxNumChops) {
            return ((IChoppable) block).withChops(blockState, newNumChops);
        } else {
            return (destructive)
                    ? Blocks.AIR.getDefaultState()
                    : ((IChoppable) block).withChops(blockState, maxNumChops);
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

    private static IChoppable getChoppedBlock(World world, BlockPos pos, IBlockState blockState) {
        return getChoppedBlock(world, pos, blockState, ChoppedLogShape.PILLAR);
    }

    public static int getNumChops(World world, BlockPos pos) {
        return getNumChops(world.getBlockState(pos));
    }

    public static int getNumChops(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? ((IChoppable) block).getNumChops(blockState) : 0;
    }

    public static int getNumChops(World world, Set<BlockPos> nearbyChoppableBlocks) {
        return nearbyChoppableBlocks.stream()
                .map(world::getBlockState)
                .map(blockState1 -> blockState1.getBlock() instanceof IChoppable
                        ? ((IChoppable) blockState1.getBlock()).getNumChops(blockState1)
                        : 0
                )
                .reduce(Integer::sum)
                .orElse(0);
    }

    private static ChopResult tryToChopWithoutFelling(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool) {
        return (isBlockChoppable(world, blockPos))
                ? new ChopResult(Collections.singletonList(
                        new TreeBlock(world, blockPos, getBlockStateAfterChops(world, blockPos, numChops, true))
                ), false)
                : ChopResult.IGNORED;
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
        if (ConfigHandler.canChooseNotToChop) {
            return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
        } else {
            return true;
        }
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

    public static void dropExperience(World world, BlockPos blockPos, int amount) {
        if (world instanceof WorldServer) {
            Blocks.AIR.dropXpOnBlockBreak(world, blockPos, amount);
        }
    }

}
