package ht.treechop.common.block;

import ht.treechop.client.model.ForgeChoppedLogBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForgeChoppedLogBlock extends ChoppedLogBlock {

    public ForgeChoppedLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        return new MyEntity(pos, blockState);
    }

    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        return (float)Math.min(0.35, getImitatedBlockState(level, pos).getDestroyProgress(player, level, pos));
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return getImitatedBlockState(level, pos).getCloneItemStack(target, level, pos, player);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState) {
        return getImitatedBlockState(level, pos).getBlock().getCloneItemStack(level, pos, blockState);
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
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getImitatedBlockState(level, pos).getSoundType(level, pos, entity);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getImitatedBlockState(level, pos).canEntityDestroy(level, pos, entity);
    }

    public static class MyEntity extends ChoppedLogBlock.MyEntity {
        public MyEntity(BlockPos pos, BlockState blockState) {
            super(pos, blockState);
        }

        @Override
        public @NotNull ModelData getModelData() {
            return ForgeChoppedLogBakedModel.getModelData(this);
        }

        @Override
        protected void rerender() {
            super.rerender();
            requestModelDataUpdate();
        }
    }
}
