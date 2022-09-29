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
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).propagatesSkylightDown(level, pos);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, Random random) {
        getImitatedBlockState(level, pos).getBlock().animateTick(blockState, level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        getImitatedBlockState(level, pos).getBlock().stepOn(level, pos, blockState, entity);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos pos, Entity entity, float speed) {
        getImitatedBlockState(level, pos).getBlock().fallOn(level, blockState, pos, entity, speed);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState blockState) {
        return getImitatedBlockState(level, pos).getBlock().getCloneItemStack(level, pos, blockState);
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        getImitatedBlockState(level, pos).getBlock().handlePrecipitation(blockState, level, pos, precipitation);
    }

    @Override
    public int getLightBlock(BlockState blockState, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).getBlock().getLightBlock(blockState, level, pos);
    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).getShadeBrightness(level, pos);
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
