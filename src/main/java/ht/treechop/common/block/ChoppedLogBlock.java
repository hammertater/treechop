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
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static ht.treechop.common.util.ChopUtil.isBlockChoppable;

public class ChoppedLogBlock extends Block implements IChoppable {

    private static final int MAX_NUM_CHOPS = 7;

    private static final PropertyEnum<ChoppedLogShape> SHAPE = BlockStateProperties.CHOPPED_LOG_SHAPE;
    private static final PropertyInteger CHOPS = BlockStateProperties.CHOP_COUNT;

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
