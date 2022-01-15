package ht.treechop.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

abstract public class BlockImitator extends Block {

    public BlockImitator(Properties properties) {
        super(properties);
    }

    abstract public BlockState getImitatedBlockState(BlockGetter level, BlockPos pos);

    @SuppressWarnings("deprecation")
    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).getDestroyProgress(player, level, pos);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        return getImitatedBlockState(level, pos).getFriction(level, pos, entity);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getImitatedBlockState(level, pos).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).getLightEmission(level, pos);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return getImitatedBlockState(level, pos).isLadder(level, pos, entity);
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return getImitatedBlockState(level, pos).isBurning(level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return getImitatedBlockState(level, pos).canHarvestBlock(level, pos, player);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getImitatedBlockState(level, pos).getPickBlock(target, level, pos, player);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getImitatedBlockState(level, pos).getSoundType(level, pos, entity);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getImitatedBlockState(level, pos).getFlammability(level, pos, face);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getImitatedBlockState(level, pos).isFlammable(level, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getImitatedBlockState(level, pos).getFireSpreadSpeed(level, pos, face);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return getImitatedBlockState(level, pos).isFireSource(level, pos, side);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getImitatedBlockState(level, pos).canEntityDestroy(level, pos, entity);
    }

}
