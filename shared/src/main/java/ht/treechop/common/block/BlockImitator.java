package ht.treechop.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public abstract class BlockImitator extends Block {
    public BlockImitator(Properties properties) {
        super(properties);
    }

    public abstract BlockState getImitatedBlockState(BlockGetter level, BlockPos pos);

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        BlockState imitatedBlockState = getImitatedBlockState(level, pos);
        imitatedBlockState.getBlock().animateTick(imitatedBlockState, level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        BlockState imitatedBlockState = getImitatedBlockState(level, pos);
        imitatedBlockState.getBlock().stepOn(level, pos, imitatedBlockState, entity);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, float speed) {
        BlockState imitatedBlockState = getImitatedBlockState(level, pos);
        imitatedBlockState.getBlock().fallOn(level, imitatedBlockState, pos, entity, speed);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState blockState) {
        BlockState imitatedBlockState = getImitatedBlockState(level, pos);
        return imitatedBlockState.getBlock().getCloneItemStack(level, pos, imitatedBlockState);
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        BlockState imitatedBlockState = getImitatedBlockState(level, pos);
        imitatedBlockState.getBlock().handlePrecipitation(imitatedBlockState, level, pos, precipitation);
    }

    @Override
    public int getLightBlock(BlockState blockState, BlockGetter level, BlockPos pos) {
        return super.getLightBlock(blockState, level, pos);
    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter level, BlockPos pos) {
        return super.getShadeBrightness(blockState, level, pos);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return getImitatedBlockState(level, pos).getAnalogOutputSignal(level, pos);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, Random random) {
        getImitatedBlockState(level, pos).randomTick(level, pos, random);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos pos, Random random) {
        getImitatedBlockState(level, pos).tick(level, pos, random);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return getImitatedBlockState(level, pos).getSignal(level, pos, direction);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return getImitatedBlockState(level, pos).getDirectSignal(level, pos, direction);
    }
}
