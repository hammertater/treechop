package ht.treechop.block;

import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.Sound;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.World;

import java.util.stream.Stream;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

public class ChoppedLogBlock extends Block implements IChoppable {

    protected static final PropertyInteger CHOPS = BlockStateProperties.CHOP_COUNT;
    protected static final PropertyEnum<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;

    public static final AxisAlignedBB[] PILLAR_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops, 0, chops, 16 - chops,
                    16, 16 - chops
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] CORNER_NW_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, 0,
                    16 - chops * 2, 16, 16 - chops * 2
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] CORNER_NE_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops * 2, 0, 0,
                    16, 16, 16 - chops * 2
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] CORNER_SE_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops * 2, 0, chops * 2,
                    16, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] CORNER_SW_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, chops * 2,
                    16 - chops * 2, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] END_W_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops * 2, 0, chops,
                    16, 16, 16 - chops
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] END_N_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops, 0, chops * 2,
                    16 - chops, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] END_E_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, chops,
                    16 - chops * 2, 16, 16 - chops
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] END_S_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops, 0, 0,
                    16 - chops, 16, 16 - chops * 2
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] SIDE_W_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    chops * 2, 0, 0,
                    16, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] SIDE_N_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, chops * 2,
                    16, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] SIDE_E_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, 0,
                    16 - chops * 2, 16, 16
            ))
            .toArray(AxisAlignedBB[]::new);

    public static final AxisAlignedBB[] SIDE_S_SHAPES_BY_CHOPS = Stream.of(0, 1, 2, 3, 4, 5, 6, 7)
            .map(chops -> new AxisAlignedBB(
                    0, 0, 0,
                    16, 16, 16 - chops * 2
            ))
            .toArray(AxisAlignedBB[]::new);

    public ChoppedLogBlock(Material material, float hardnessAndResistance, SoundType soundType) {
        super(material);
        this.setDefaultState(this.getDefaultState()
                .withProperty(CHOPS, 1)
                .withProperty(SHAPE, ChoppedLogShape.PILLAR)
        );
        this.setSoundType(soundType);
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    public static ChoppedLogShape getPlacementShape(World world, BlockPos blockPos) {
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int chops = state.getValue(CHOPS);
        switch (state.getValue(SHAPE)) {
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
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(CHOPS, SHAPE);
    }

    @Override
    public IBlockState withChops(IBlockState blockState, int numChops) {
        if (numChops > getMaxNumChops()) {
            throw new IllegalArgumentException("Too many chops");
        }
        return blockState.with(CHOPS, numChops);
    }

    @Override
    public int getNumChops(IBlockState blockState) {
        return blockState.get(CHOPS);
    }

    @Override
    public int getMaxNumChops() {
        return 7;
    }
}
