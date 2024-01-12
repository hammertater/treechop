package ht.treechop.common.block;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.server.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static ht.treechop.common.chop.ChopUtil.isBlockALog;
import static ht.treechop.common.chop.ChopUtil.isBlockLeaves;

@SuppressWarnings("NullableProblems")
public abstract class ChoppedLogBlock extends BlockImitator implements IChoppableBlock, EntityBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final int DEFAULT_SUPPORT_FACTOR = 7;
    public static final int DEFAULT_MAX_NUM_CHOPS = 7;
    public static final int DEFAULT_UNCHOPPED_RADIUS = 8;

    public ChoppedLogBlock(BlockBehaviour.Properties properties) {
        super(properties
                .dynamicShape()
                .isViewBlocking((BlockState blockState, BlockGetter level, BlockPos pos) -> false));
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(WATERLOGGED, Boolean.FALSE)
        );
    }

    public static ChoppedLogShape getPlacementShape(Level level, BlockPos blockPos, BlockState state) {
        final byte DOWN = 1;
        final byte UP = 1 << 1;
        final byte NORTH = 1 << 2;
        final byte SOUTH = 1 << 3;
        final byte WEST = 1 << 4;
        final byte EAST = 1 << 5;

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
        return (level.isEmptyBlock(pos) || isBlockLeaves(level, pos));
    }

    public BlockState getImitatedBlockState(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MyEntity entity && !(entity.getOriginalState().is(this))) {
            return entity.getOriginalState();
        } else {
            return Blocks.OAK_LOG.defaultBlockState();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        return (float) Math.min(0.35, getImitatedBlockState(level, pos).getDestroyProgress(player, level, pos));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        final double scale = 1.0 / 16.0;
        if (level.getBlockEntity(pos) instanceof MyEntity entity) {
            AABB box = entity.getShape().getBoundingBox(entity.getRadius());
            return Shapes.box(
                    box.minX * scale,
                    box.minY * scale,
                    box.minZ * scale,
                    box.maxX * scale,
                    box.maxY * scale,
                    box.maxZ * scale
            );
        } else {
            return Shapes.block();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity && entity.getOriginalState().isSolidRender(level, pos)) {
            return entity.getShape().getOcclusionShape();
        } else {
            return Shapes.empty();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        return (level.getBlockEntity(pos) instanceof MyEntity entity) ? entity.getChops() : 0;
    }

    @Override
    public int getMaxNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        if (level.getBlockEntity(pos) instanceof MyEntity entity) {
            return entity.getMaxNumChops();
        } else {
            return DEFAULT_MAX_NUM_CHOPS;
        }
    }

    @Override
    public double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
        if (level.getBlockEntity(pos) instanceof MyEntity entity) {
            return entity.getSupportFactor();
        } else {
            return DEFAULT_SUPPORT_FACTOR;
        }
    }

    @Override
    public void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
        int currentNumChops = ChopUtil.getNumChops(level, pos, blockState);
        int maxNumChops = ChopUtil.getMaxNumChops(level, pos, blockState);
        int newNumChops = Math.min(currentNumChops + numChops, maxNumChops);
        int numAddedChops = newNumChops - currentNumChops;

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < numAddedChops; ++i) {
                getDrops(defaultBlockState(), serverLevel, pos, null, player, tool)
                        .forEach(stack -> popResource(serverLevel, pos, stack));
            }
        }

        if (!felling) {
            if (numAddedChops > 0) {
                if (!blockState.is(this)) {
                    int chopZeroRadius = Optional.ofNullable(ClassUtil.getCylinderBlock(blockState.getBlock()))
                            .map(slimBlock -> slimBlock.getRadius(level, pos, blockState))
                            .orElse(8);

                    double supportFactor = Optional.ofNullable(ClassUtil.getFellableBlock(blockState.getBlock()))
                            .map(fellableBlock -> fellableBlock.getSupportFactor(level, pos, blockState))
                            .orElse(1.0);

                    BlockState newBlockState = (blockState.is(this) ? blockState : getPlacementState(level, pos));
                    if (level.setBlockAndUpdate(pos, newBlockState)
                            && level.getBlockEntity(pos) instanceof MyEntity entity
                            && level instanceof ServerLevel serverLevel) {
                        entity.setShape(getPlacementShape(level, pos, blockState));
                        entity.setOriginalState(blockState);
                        entity.setParameters(chopZeroRadius, maxNumChops, supportFactor);

                        List<ItemStack> drops = Block.getDrops(blockState, serverLevel, pos, entity, player, tool);
                        if (ConfigHandler.COMMON.dropLootOnFirstChop.get()) {
                            drops.forEach(stack -> popResource(level, pos, stack));
                        } else {
                            entity.setDrops(drops);
                        }
                    }
                }

                if (level.getBlockEntity(pos) instanceof MyEntity entity) {
                    entity.setChops(newNumChops);
                    entity.setChanged();
                }
            } else {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getPlacementState(context.getLevel(), context.getClickedPos());
    }

    private BlockState getPlacementState(Level level, BlockPos pos) {
        return defaultBlockState()
                .setValue(WATERLOGGED, shouldPlaceAsWaterlogged(level, pos));
    }

    private boolean shouldPlaceAsWaterlogged(Level level, BlockPos pos) {
        final Direction[] waterSourceDirections = {
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST,
                Direction.UP
        };

        return Arrays.stream(waterSourceDirections)
                .filter(direction -> level.getFluidState(pos.offset(direction.getNormal())).isSource())
                .limit(2)
                .count() == 2;
    }

    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState blockState, Direction side, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (blockState.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, side, neighborState, level, pos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder context) {
        if (ConfigHandler.COMMON.dropLootForChoppedBlocks.get() && context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MyEntity entity) {
            return entity.drops;
        } else {
            return Collections.emptyList();
        }
    }

    public static abstract class MyEntity extends BlockEntity {
        public static final String KEY_CHOPS = "Chops";
        public static final String KEY_SHAPE = "Shape";
        public static final String KEY_DROPS = "Drops";
        public static final String KEY_ORIGINAL_STATE = "OriginalState";
        public static final String KEY_UNCHOPPED_RADIUS = "UnchoppedRadius";
        public static final String KEY_MAX_NUM_CHOPS = "MaxNumChops";
        public static final String KEY_SUPPORT_FACTOR = "SupportFactor";

        protected BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
        protected List<ItemStack> drops = Collections.emptyList();
        private ChoppedLogShape shape = ChoppedLogShape.PILLAR_Y;
        private int chops = 1;

        private int unchoppedRadius = DEFAULT_UNCHOPPED_RADIUS;
        private int maxNumChops = DEFAULT_MAX_NUM_CHOPS;
        private double supportFactor = DEFAULT_SUPPORT_FACTOR;

        public MyEntity(BlockPos pos, BlockState blockState) {
            super(TreeChop.platform.getChoppedLogBlockEntity(), pos, blockState);
        }

        /**
         * @param unchoppedRadius Maximum is 8 (default), minimum is 1.
         */
        public void setParameters(int unchoppedRadius, int maxNumChops, double supportFactor) {
            this.unchoppedRadius = unchoppedRadius;
            this.maxNumChops = maxNumChops;
            this.supportFactor = supportFactor;
        }

        public void setDrops(List<ItemStack> drops) {
            this.drops = drops;
        }

        public int getChops() {
            return chops;
        }

        public void setChops(int chops) {
            this.chops = chops;
        }

        public ChoppedLogShape getShape() {
            return shape;
        }

        public void setShape(ChoppedLogShape shape) {
            this.shape = shape;
        }

        public BlockState getOriginalState() {
            return originalState;
        }

        public void setOriginalState(BlockState originalState) {
            this.originalState = originalState;
        }

        public int getUnchoppedRadius() {
            return unchoppedRadius;
        }

        public int getRadius() {
            return Math.max(getUnchoppedRadius() - getChops(), 1);
        }

        public int getMaxNumChops() {
            return maxNumChops;
        }

        public double getSupportFactor() {
            return supportFactor;
        }

        @Override
        public void saveAdditional(@Nonnull CompoundTag tag) {
            super.saveAdditional(tag);

            tag.putInt(KEY_ORIGINAL_STATE, Block.getId(getOriginalState()));
            tag.putInt(KEY_CHOPS, getChops());
            tag.putInt(KEY_SHAPE, getShape().ordinal());

            if (unchoppedRadius != DEFAULT_UNCHOPPED_RADIUS) {
                tag.putInt(KEY_UNCHOPPED_RADIUS, unchoppedRadius);
            }

            if (maxNumChops != DEFAULT_MAX_NUM_CHOPS) {
                tag.putInt(KEY_MAX_NUM_CHOPS, maxNumChops);
            }

            if (supportFactor != DEFAULT_SUPPORT_FACTOR) {
                tag.putDouble(KEY_SUPPORT_FACTOR, supportFactor);
            }

            ListTag list = new ListTag();
            drops.stream().map(stack -> stack.save(new CompoundTag()))
                    .forEach(list::add);
            tag.put(KEY_DROPS, list);
        }

        @Override
        public void load(@Nonnull CompoundTag tag) {
            super.load(tag);

            int stateId = tag.getInt(KEY_ORIGINAL_STATE);
            setOriginalState(stateId > 0 ? Block.stateById(stateId) : Blocks.OAK_LOG.defaultBlockState());

            setChops(tag.getInt(KEY_CHOPS));
            setShape(ChoppedLogShape.values()[tag.getInt(KEY_SHAPE)]);

            int unchoppedRadius = (tag.contains(KEY_UNCHOPPED_RADIUS)) ? tag.getInt(KEY_UNCHOPPED_RADIUS) : DEFAULT_UNCHOPPED_RADIUS;
            int maxNumChops = (tag.contains(KEY_MAX_NUM_CHOPS)) ? tag.getInt(KEY_MAX_NUM_CHOPS) : DEFAULT_MAX_NUM_CHOPS;
            double supportFactor = (tag.contains(KEY_SUPPORT_FACTOR)) ? tag.getInt(KEY_SUPPORT_FACTOR) : DEFAULT_SUPPORT_FACTOR;
            setParameters(unchoppedRadius, maxNumChops, supportFactor);

            ListTag list = tag.getList(KEY_DROPS, 10);

            drops = new LinkedList<>();
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag item = list.getCompound(i);
                drops.add(ItemStack.of(item));
            }

            rerender();
        }

        private void rerender() {
            if (level != null) {
                level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
            }
        }

        @Nonnull
        @Override
        public CompoundTag getUpdateTag() {
            return saveWithoutMetadata();
        }

        @Nullable
        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this); // calls getUpdateTag
        }

        @Override
        public void setChanged() {
            super.setChanged();
            syncWithClients();
        }

        public void syncWithClients() {
            if (level instanceof ServerLevel serverLevel) {
                Server.instance().broadcast(serverLevel, worldPosition, new ServerUpdateChopsPacket(worldPosition, getUpdateTag()));
            }
        }

        @Override
        public void setLevel(@NotNull Level level) {
            super.setLevel(level);
            if (level.isClientSide()) {
                update();
            }
        }

        public void update() {
            CompoundTag update = ServerUpdateChopsPacket.getPendingUpdate(level, worldPosition);
            if (update != null) {
                load(update);
            }
        }
    }

}
