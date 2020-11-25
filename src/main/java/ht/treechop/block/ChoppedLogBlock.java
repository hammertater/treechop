package ht.treechop.block;

import ht.treechop.TreeChopMod;
import ht.treechop.config.ConfigHandler;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import ht.treechop.util.BlockNeighbors;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ht.treechop.util.ChopUtil.chipBlock;
import static ht.treechop.util.ChopUtil.getConnectedBlocksMatchingCondition;
import static ht.treechop.util.ChopUtil.isBlockALog;
import static ht.treechop.util.ChopUtil.isBlockChoppable;

public class ChoppedLogBlock extends Block {

    public static final IntegerProperty CHOPS = BlockStateProperties.CHOP_COUNT;
    public static final EnumProperty<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;

    public static final VoxelShape[] PILLAR_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops, 0, chops, 16 - chops,
                    16, 16 - chops
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] CORNER_NW_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, 0,
                    16 - chops * 2, 16, 16 - chops * 2
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] CORNER_NE_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops * 2, 0, 0,
                    16, 16, 16 - chops * 2
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] CORNER_SE_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops * 2, 0, chops * 2,
                    16, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] CORNER_SW_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, chops * 2,
                    16 - chops * 2, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] END_W_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops * 2, 0, chops,
                    16, 16, 16 - chops
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] END_N_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops, 0, chops * 2,
                    16 - chops, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] END_E_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, chops,
                    16 - chops * 2, 16, 16 - chops
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] END_S_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops, 0, 0,
                    16 - chops, 16, 16 - chops * 2
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] SIDE_W_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    chops * 2, 0, 0,
                    16, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] SIDE_N_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, chops * 2,
                    16, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] SIDE_E_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, 0,
                    16 - chops * 2, 16, 16
            ))
            .toArray(VoxelShape[]::new);

    public static final VoxelShape[] SIDE_S_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> Block.makeCuboidShape(
                    0, 0, 0,
                    16, 16, 16 - chops * 2
            ))
            .toArray(VoxelShape[]::new);

    public ChoppedLogBlock(Properties properties) {
        super(properties);
        this.setDefaultState(
                this.stateContainer.getBaseState()
                        .with(CHOPS, 1)
                        .with(SHAPE, ChoppedLogShape.PILLAR)
        );
    }

    public static ChoppedLogShape getPlacementShape(IWorld world, BlockPos blockPos) {
        final byte NORTH = 0b0001;
        final byte EAST = 0b0010;
        final byte SOUTH = 0b0100;
        final byte WEST = 0b1000;

        byte sides = (byte) (
                (isBlockChoppable(world, blockPos.north()) ? NORTH : 0) |
                        (isBlockChoppable(world, blockPos.east()) ? EAST : 0) |
                        (isBlockChoppable(world, blockPos.south()) ? SOUTH : 0) |
                        (isBlockChoppable(world, blockPos.west()) ? WEST : 0)
        );

        switch (sides) {
            case NORTH | WEST:
                return ChoppedLogShape.CORNER_NW;
            case NORTH | EAST:
                return ChoppedLogShape.CORNER_NE;
            case SOUTH | EAST:
                return ChoppedLogShape.CORNER_SE;
            case SOUTH | WEST:
                return ChoppedLogShape.CORNER_SW;
            case EAST:
                return ChoppedLogShape.END_W;
            case SOUTH:
                return ChoppedLogShape.END_N;
            case WEST:
                return ChoppedLogShape.END_E;
            case NORTH:
                return ChoppedLogShape.END_S;
            case NORTH | EAST | SOUTH:
                return ChoppedLogShape.SIDE_W;
            case EAST | SOUTH | WEST:
                return ChoppedLogShape.SIDE_N;
            case SOUTH | WEST | NORTH:
                return ChoppedLogShape.SIDE_E;
            case WEST | NORTH | EAST:
                return ChoppedLogShape.SIDE_S;
            default:
                return ChoppedLogShape.PILLAR;
        }
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int chops = state.get(CHOPS);
        switch (state.get(SHAPE)) {
            case CORNER_NW:
                return CORNER_NW_SHAPES_BY_CHOPS[chops];
            case CORNER_NE:
                return CORNER_NE_SHAPES_BY_CHOPS[chops];
            case CORNER_SE:
                return CORNER_SE_SHAPES_BY_CHOPS[chops];
            case CORNER_SW:
                return CORNER_SW_SHAPES_BY_CHOPS[chops];
            case END_W:
                return END_W_SHAPES_BY_CHOPS[chops];
            case END_N:
                return END_N_SHAPES_BY_CHOPS[chops];
            case END_E:
                return END_E_SHAPES_BY_CHOPS[chops];
            case END_S:
                return END_S_SHAPES_BY_CHOPS[chops];
            case SIDE_W:
                return SIDE_W_SHAPES_BY_CHOPS[chops];
            case SIDE_N:
                return SIDE_N_SHAPES_BY_CHOPS[chops];
            case SIDE_E:
                return SIDE_E_SHAPES_BY_CHOPS[chops];
            case SIDE_S:
                return SIDE_S_SHAPES_BY_CHOPS[chops];
            default:
                return PILLAR_SHAPES_BY_CHOPS[chops];
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CHOPS, SHAPE);
    }

    public void chop(IWorld world, BlockPos blockPos, BlockState blockState, Entity agent, int numChops) {
        Set<BlockPos> nearbyChoppableBlocks;
        Set<BlockPos> supportedBlocks = getConnectedBlocksMatchingCondition(
                Collections.singletonList(blockPos),
                BlockNeighbors.HORIZONTAL_AND_ABOVE,
                checkPos -> isBlockALog(world, checkPos),
                ConfigHandler.maxNumTreeBlocks
        );

        if (supportedBlocks.size() >= ConfigHandler.maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d blocks", supportedBlocks.size(), ConfigHandler.maxNumTreeBlocks));
        }

        int numChopsToFell = ChopUtil.numChopsToFell(supportedBlocks.size());

        if (blockState.get(CHOPS) + numChops >= numChopsToFell) {
            ChopUtil.fellTree(world, supportedBlocks, agent);
        } else {
            nearbyChoppableBlocks = getConnectedBlocksMatchingCondition(
                    Collections.singletonList(blockPos),
                    BlockNeighbors.ADJACENTS_AND_DIAGONALS,
                    checkPos -> Math.abs(checkPos.getY() - blockPos.getY()) < 4 && isBlockChoppable(world, checkPos),
                    64
            );

            int totalNumChops = nearbyChoppableBlocks.stream()
                    .map(world::getBlockState)
                    .filter(blockState1 -> blockState1.func_235901_b_(CHOPS))
                    .map(blockState2 -> blockState2.get(CHOPS))
                    .reduce(Integer::sum)
                    .orElse(0) + numChops; // Include this chop

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof ChoppedLogBlock)
                        .sorted(Comparator.comparingInt(Vector3i::getY))
                        .collect(Collectors.toList());

                int chops = 0;
                for (BlockPos pos : choppedLogsSortedByY) {
                    chops += world.getBlockState(pos).get(CHOPS);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }

                ChopUtil.fellTree(world, supportedBlocks, agent);
            } else {
                int newNumChops = blockState.get(CHOPS) + numChops;
                if (newNumChops < 8) {
                    world.setBlockState(blockPos, blockState.with(CHOPS, newNumChops), 3); // p_180501_3_=3 means do this on client and server
                } else { // If this block is out of chops, chop another block
                    List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                            .filter(blockPos1 -> {
                                BlockState blockState1 = world.getBlockState(blockPos1);
                                if (blockState1.getBlock() instanceof ChoppedLogBlock) {
                                    return blockState1.get(CHOPS) < 7;
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
                        BlockPos otherPos = sortedChoppableBlocks.get(Math.floorMod(RANDOM.nextInt(), choiceIndexLimit));
                        BlockState otherBlockState = world.getBlockState(otherPos);
                        if (otherBlockState.getBlock() instanceof ChoppedLogBlock) {
                            world.setBlockState(otherPos, otherBlockState.with(CHOPS, otherBlockState.get(CHOPS) + numChops), 3);
                        } else {
                            chipBlock(world, otherPos, numChops, agent);
                        }
                    } else {
                        world.destroyBlock(blockPos, true, agent);
                    }
                }
            }
        }
    }

    public int chopDistance(BlockPos a, BlockPos b) {
        return a.manhattanDistance(b);
    }
}
