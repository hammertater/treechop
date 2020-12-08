package ht.treechop.block;

import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.stream.Stream;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

public class ChoppedLogBlock extends Block implements IChoppable {

    protected static final IntegerProperty CHOPS = BlockStateProperties.CHOP_COUNT;
    protected static final EnumProperty<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;

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

    @SuppressWarnings("deprecation")
    @Override
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

    @Override
    public BlockState withChops(BlockState blockState, int numChops) {
        if (numChops > getMaxNumChops()) {
            throw new IllegalArgumentException("Too many chops");
        }
        return blockState.with(CHOPS, numChops);
    }

    @Override
    public int getNumChops(BlockState blockState) {
        return blockState.get(CHOPS);
    }

    @Override
    public int getMaxNumChops() {
        return 7;
    }
}
