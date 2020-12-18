package ht.treechop.common.block;

import ht.treechop.TreeChopMod;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ht.treechop.common.util.ChopUtil.isBlockChoppable;

public class ChoppedLogBlock extends Block implements IChoppable {

    private static final int MAX_NUM_CHOPS = 7;

    private static final PropertyEnum<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;
    private static final PropertyInteger CHOPS = BlockStateProperties.CHOP_COUNT;

    public static final AxisAlignedBB[] PILLAR_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] CORNER_NW_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] CORNER_NE_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] CORNER_SE_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] CORNER_SW_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] END_W_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] END_N_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] END_E_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] END_S_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] SIDE_W_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] SIDE_N_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] SIDE_E_SHAPES_BY_CHOPS;
    public static final AxisAlignedBB[] SIDE_S_SHAPES_BY_CHOPS;

    static {
        List<Float> scaledChops = Stream.of(0F, 1F, 2F, 3F, 4F, 5F, 6F, 7F).map(a -> a / 16).collect(Collectors.toList());
        PILLAR_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops, 0, chops, 1 - chops,
                        1, 1 - chops
                ))
                .toArray(AxisAlignedBB[]::new);

        CORNER_NW_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, 0,
                        1 - chops * 2, 1, 1 - chops * 2
                ))
                .toArray(AxisAlignedBB[]::new);

        CORNER_NE_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops * 2, 0, 0,
                        1, 1, 1 - chops * 2
                ))
                .toArray(AxisAlignedBB[]::new);

        CORNER_SE_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops * 2, 0, chops * 2,
                        1, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        CORNER_SW_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, chops * 2,
                        1 - chops * 2, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        END_W_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops * 2, 0, chops,
                        1, 1, 1 - chops
                ))
                .toArray(AxisAlignedBB[]::new);

        END_N_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops, 0, chops * 2,
                        1 - chops, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        END_E_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, chops,
                        1 - chops * 2, 1, 1 - chops
                ))
                .toArray(AxisAlignedBB[]::new);

        END_S_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops, 0, 0,
                        1 - chops, 1, 1 - chops * 2
                ))
                .toArray(AxisAlignedBB[]::new);

        SIDE_W_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        chops * 2, 0, 0,
                        1, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        SIDE_N_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, chops * 2,
                        1, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        SIDE_E_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, 0,
                        1 - chops * 2, 1, 1
                ))
                .toArray(AxisAlignedBB[]::new);

        SIDE_S_SHAPES_BY_CHOPS = scaledChops.stream()
                .map(chops -> new AxisAlignedBB(
                        0, 0, 0,
                        1, 1, 1 - chops * 2
                ))
                .toArray(AxisAlignedBB[]::new);
    }

    public ChoppedLogBlock() {
        super(Material.WOOD, MapColor.WOOD);
        setRegistryName(new ResourceLocation(TreeChopMod.MOD_ID, "chopped_log"));
        setSoundType(SoundType.WOOD);
        setHardness(2.0F);
        setResistance(2.0F);
        setDefaultState(getBlockState().getBaseState()
                .withProperty(SHAPE, ChoppedLogShape.PILLAR)
                .withProperty(CHOPS, 1)
        );
    }

    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
                .withProperty(CHOPS, meta);
    }

    @Override
    public int getMetaFromState(IBlockState blockState)
    {
        return blockState.getValue(CHOPS);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, SHAPE, CHOPS);
    }

    @Override
    public IBlockState withChops(IBlockState blockState, int numChops) {
        return blockState.withProperty(CHOPS, numChops);
    }

    @Override
    public int getNumChops(IBlockState blockState) {
        return blockState.getValue(CHOPS);
    }

    @Override
    public int getMaxNumChops() {
        return MAX_NUM_CHOPS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        double[] edges;
        boolean flush;
        AxisAlignedBB box = getBoundingBox(state, null, pos);
        switch (face) {
            case WEST:
            case EAST:
                edges = new double[] {box.minY, box.minZ, box.maxY, box.maxZ};
                flush = (face == EnumFacing.WEST) ? box.minX == 0 : box.maxX == 1;
                break;
            case NORTH:
            case SOUTH:
                edges = new double[] {box.minY, box.minX, box.maxY, box.maxX};
                flush = (face == EnumFacing.NORTH) ? box.minZ == 0 : box.maxZ == 1;
                break;
            case DOWN:
            case UP:
                edges = new double[] {box.minX, box.minZ, box.maxX, box.maxZ};
                flush = (face == EnumFacing.DOWN) ? box.minY == 0 : box.maxY == 1;
                break;
            default:
                return BlockFaceShape.UNDEFINED;
        }

        if (!flush) {
            return BlockFaceShape.UNDEFINED;
        }

        double minDistanceFromCenter = Stream.of(
                0.5 - edges[0],
                0.5 - edges[1],
                edges[2] - 0.5,
                edges[3] - 0.5
        ).reduce(BinaryOperator.minBy(Double::compare)).orElse(0.0);

        return (minDistanceFromCenter > 0) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
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

}
