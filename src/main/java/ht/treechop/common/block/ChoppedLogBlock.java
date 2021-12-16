package ht.treechop.common.block;

import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static ht.treechop.common.util.ChopUtil.isBlockLeaves;

public class ChoppedLogBlock extends Block implements IChoppableBlock {

    protected static final IntegerProperty CHOPS = BlockStateProperties.CHOP_COUNT;
    protected static final EnumProperty<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;

    public ChoppedLogBlock(Properties properties) {
        super(properties
                .dynamicShape()
                .isViewBlocking((BlockState blockState, BlockGetter level, BlockPos pos) -> false));
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(CHOPS, 1)
                        .setValue(SHAPE, ChoppedLogShape.PILLAR_Y)
        );
    }

    public static ChoppedLogShape getPlacementShape(Level level, BlockPos blockPos) {
        final byte DOWN     = 1;
        final byte UP       = 1 << 1;
        final byte NORTH    = 1 << 2;
        final byte SOUTH    = 1 << 3;
        final byte WEST     = 1 << 4;
        final byte EAST     = 1 << 5;

        byte openSides = (byte) (
                (isBlockOpen(level, blockPos.below()) ? DOWN : 0)
                | (!isBlockALog(level, blockPos.above()) ? UP : 0)
                | (!isBlockALog(level, blockPos.north()) ? NORTH : 0)
                | (!isBlockALog(level, blockPos.south()) ? SOUTH : 0)
                | (!isBlockALog(level, blockPos.west()) ? WEST : 0)
                | (!isBlockALog(level, blockPos.east()) ? EAST : 0)
        );

        return ChoppedLogShape.forOpenSides(openSides);
    }

    private static boolean isBlockOpen(Level level, BlockPos pos) {
        return (level.isEmptyBlock(pos.below()) || isBlockLeaves(level, pos.below()));
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        int chops = state.getValue(CHOPS);
        AABB box = state.getValue(SHAPE).getBoundingBox(chops);
        VoxelShape voxelShape = Shapes.box(
                box.minX,
                box.minY,
                box.minZ,
                box.maxX,
                box.maxY,
                box.maxZ
        );
        return voxelShape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHOPS, SHAPE);
    }

    @Override
    public BlockState withChops(BlockState blockState, int numChops) {
        if (numChops > getMaxNumChops()) {
            throw new IllegalArgumentException("Too many chops");
        }
        return blockState.setValue(CHOPS, numChops);
    }

    @Override
    public int getNumChops(BlockState blockState) {
        return blockState.getValue(CHOPS);
    }

    @Override
    public int getMaxNumChops() {
        return 7;
    }


}
