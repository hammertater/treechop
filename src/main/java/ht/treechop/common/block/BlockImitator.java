package ht.treechop.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

abstract public class BlockImitator extends Block {

    public BlockImitator(Properties properties) {
        super(properties);
    }

    abstract public BlockState getImitatedBlockState(IBlockReader world, BlockPos pos);

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).propagatesSkylightDown(world, pos);
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        getImitatedBlockState(world, pos).getBlock().stepOn(world, pos, entity);
    }

    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float p_180658_4_) {
        getImitatedBlockState(world, pos).getBlock().fallOn(world, pos, entity, p_180658_4_);
    }

    @Override
    public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getImitatedBlockState(world, pos).getSlipperiness(world, pos, entity);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).getLightValue(world, pos);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return getImitatedBlockState(world, pos).isLadder(world, pos, entity);
    }

    @Override
    public boolean isAir(BlockState state, IBlockReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).isAir(world, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return getImitatedBlockState(world, pos).getExplosionResistance(world, pos, explosion);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return getImitatedBlockState(world, pos).canConnectRedstone(world, pos, side);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getImitatedBlockState(world, pos).getPickBlock(target, world, pos, player);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld world, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return getImitatedBlockState(world, pos).addLandingEffects(world, pos, state2, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return getImitatedBlockState(world, pos).addRunningEffects(world, pos, entity);
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return getImitatedBlockState(world, new BlockPos(target.getLocation())).addHitEffects(world, target, manager);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return getImitatedBlockState(world, pos).addDestroyEffects(world, pos, manager);
    }

    @Override
    public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).isFertile(world, pos);
    }

    @Override
    public boolean isConduitFrame(BlockState state, IWorldReader world, BlockPos pos, BlockPos conduit) {
        return getImitatedBlockState(world, pos).isConduitFrame(world, pos, conduit);
    }

    @Override
    public boolean isPortalFrame(BlockState state, IBlockReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).isPortalFrame(world, pos);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return getImitatedBlockState(world, pos).getEnchantPowerBonus(world, pos);
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getImitatedBlockState(world, pos).getSoundType(world, pos, entity);
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return getImitatedBlockState(world, pos).getBeaconColorMultiplier(world, pos, beaconPos);
    }

    @Override
    public Vector3d getFogColor(BlockState state, IWorldReader world, BlockPos pos, Entity entity, Vector3d originalColor, float partialTicks) {
        return getImitatedBlockState(world, pos).getFogColor(world, pos, entity, originalColor, partialTicks);
    }

    @Override
    public BlockState getStateAtViewpoint(BlockState state, IBlockReader world, BlockPos pos, Vector3d viewpoint) {
        return getImitatedBlockState(world, pos).getStateAtViewpoint(world, pos, viewpoint);
    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
        return getImitatedBlockState(world, pos).getAiPathNodeType(world, pos, entity);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return getImitatedBlockState(world, pos).getFlammability(world, pos, face);
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return getImitatedBlockState(world, pos).isFlammable(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return getImitatedBlockState(world, pos).getFireSpreadSpeed(world, pos, face);
    }

    @Override
    public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return getImitatedBlockState(world, pos).isFireSource(world, pos, side);
    }

    @Override
    public boolean isScaffolding(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return getImitatedBlockState(world, pos).isScaffolding(entity);
    }
}
