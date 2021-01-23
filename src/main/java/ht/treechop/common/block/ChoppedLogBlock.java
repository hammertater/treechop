package ht.treechop.common.block;

import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import javafx.geometry.BoundingBox;
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

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static ht.treechop.common.util.ChopUtil.isBlockLeaves;

public class ChoppedLogBlock extends Block implements IChoppable {

    protected static final IntegerProperty CHOPS = BlockStateProperties.CHOP_COUNT;
    protected static final EnumProperty<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;

    public ChoppedLogBlock(Properties properties) {
        super(properties.variableOpacity());
        this.setDefaultState(
                this.stateContainer.getBaseState()
                        .with(CHOPS, 1)
                        .with(SHAPE, ChoppedLogShape.PILLAR_Y)
        );
    }

    public static ChoppedLogShape getPlacementShape(IWorld world, BlockPos blockPos) {
        final byte DOWN     = 1;
        final byte UP       = 1 << 1;
        final byte NORTH    = 1 << 2;
        final byte SOUTH    = 1 << 3;
        final byte WEST     = 1 << 4;
        final byte EAST     = 1 << 5;

        byte openSides = (byte) (
                (isBlockOpen(world, blockPos.down()) ? DOWN : 0)
                | (!isBlockALog(world, blockPos.up()) ? UP : 0)
                | (!isBlockALog(world, blockPos.north()) ? NORTH : 0)
                | (!isBlockALog(world, blockPos.south()) ? SOUTH : 0)
                | (!isBlockALog(world, blockPos.west()) ? WEST : 0)
                | (!isBlockALog(world, blockPos.east()) ? EAST : 0)
        );

        return ChoppedLogShape.forOpenSides(openSides);
    }

    private static boolean isBlockOpen(IWorld world, BlockPos pos) {
        return (world.isAirBlock(pos.down()) || isBlockLeaves(world, pos.down()));
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int chops = state.get(CHOPS);
        BoundingBox box = state.get(SHAPE).getBoundingBox(chops);
        return Block.makeCuboidShape(
                box.getMinX(),
                box.getMinY(),
                box.getMinZ(),
                box.getMaxX(),
                box.getMaxY(),
                box.getMaxZ()
        );
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(SHAPE).getOcclusionShape();
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
