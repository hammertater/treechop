package ht.treechop.common.block;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.client.Client;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.loot.TreeChopLootContextParams;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.common.util.FaceShape;
import ht.treechop.server.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

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
        if (ConfigHandler.removeBarkOnInteriorLogs.get() && level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity && entity.getOriginalState().isSolidRender(level, pos)) {
            return entity.getOcclusionShape(level, pos);
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

                        if (ConfigHandler.COMMON.dropLootOnFirstChop.get()) {
                            Block.getDrops(blockState, serverLevel, pos, entity, player, tool)
                                    .forEach(stack -> popResource(level, pos, stack));
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

        if (level instanceof ServerLevel serverLevel) {
            ResourceLocation chopLootTable = BuiltInRegistries.BLOCK.getKey(this.asBlock()).withPrefix("chopping/");
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(chopLootTable);
            int finalBlockChopCount = currentNumChops + numAddedChops;

            for (int i = 0; i < numAddedChops; ++i) {
                int blockChopCount = 1 + currentNumChops + i;
                LootParams.Builder builder = (new LootParams.Builder(serverLevel))
                        .withParameter(LootContextParams.BLOCK_STATE, defaultBlockState())
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, tool)
                        .withParameter(TreeChopLootContextParams.BLOCK_CHOP_COUNT, blockChopCount)
                        .withParameter(TreeChopLootContextParams.DESTROY_BLOCK, felling && (blockChopCount == finalBlockChopCount))
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));

                lootTable.getRandomItems(builder.create(TreeChopLootContextParams.SET))
                        .forEach(stack -> popResource(serverLevel, pos, stack));
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

        if (level.getBlockEntity(pos) instanceof MyEntity entity) {
            entity.rerender();
        }

        return super.updateShape(blockState, side, neighborState, level, pos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder context) {
        List<ItemStack> stacks = new ArrayList<>(super.getDrops(blockState, context));

        if (ConfigHandler.COMMON.dropLootForChoppedBlocks.get() && context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MyEntity entity) {
            ItemStack tool = context.getOptionalParameter(LootContextParams.TOOL);
            Entity player = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
            stacks.addAll(Block.getDrops(entity.originalState, context.getLevel(), entity.getBlockPos(), entity, player, (tool == null) ? ItemStack.EMPTY : tool));
        }

        return stacks;
    }

    public static abstract class MyEntity extends BlockEntity {
        public static final String KEY_CHOPS = "Chops";
        public static final String KEY_SHAPE = "Shape";
        public static final String KEY_ORIGINAL_STATE = "OriginalState";
        public static final String KEY_UNCHOPPED_RADIUS = "UnchoppedRadius";
        public static final String KEY_MAX_NUM_CHOPS = "MaxNumChops";
        public static final String KEY_SUPPORT_FACTOR = "SupportFactor";

        protected BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
        private ChoppedLogShape shape = ChoppedLogShape.PILLAR_Y;
        private int chops = 1;

        private int unchoppedRadius = DEFAULT_UNCHOPPED_RADIUS;
        private int maxNumChops = DEFAULT_MAX_NUM_CHOPS;
        private double supportFactor = DEFAULT_SUPPORT_FACTOR;

        public MyEntity(BlockPos pos, BlockState blockState) {
            super(TreeChop.platform.getChoppedLogBlockEntity(), pos, blockState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(level, worldPosition, originalState, shape, chops, unchoppedRadius, maxNumChops, supportFactor);
        }

        /**
         * @param unchoppedRadius Maximum is 8 (default), minimum is 1.
         */
        public void setParameters(int unchoppedRadius, int maxNumChops, double supportFactor) {
            this.unchoppedRadius = unchoppedRadius;
            this.maxNumChops = maxNumChops;
            this.supportFactor = supportFactor;
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
        }

        @Override
        public void load(@Nonnull CompoundTag tag) {
            super.load(tag);

            int hash = hashCode();

            int stateId = tag.getInt(KEY_ORIGINAL_STATE);
            setOriginalState(stateId > 0 ? Block.stateById(stateId) : Blocks.OAK_LOG.defaultBlockState());

            setChops(tag.getInt(KEY_CHOPS));
            setShape(ChoppedLogShape.values()[tag.getInt(KEY_SHAPE)]);

            int unchoppedRadius = (tag.contains(KEY_UNCHOPPED_RADIUS)) ? tag.getInt(KEY_UNCHOPPED_RADIUS) : DEFAULT_UNCHOPPED_RADIUS;
            int maxNumChops = (tag.contains(KEY_MAX_NUM_CHOPS)) ? tag.getInt(KEY_MAX_NUM_CHOPS) : DEFAULT_MAX_NUM_CHOPS;
            double supportFactor = (tag.contains(KEY_SUPPORT_FACTOR)) ? tag.getInt(KEY_SUPPORT_FACTOR) : DEFAULT_SUPPORT_FACTOR;
            setParameters(unchoppedRadius, maxNumChops, supportFactor);

            if (hash != hashCode()) {
                rerenderNeighborhood();
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

        private void syncWithClients() {
            if (level instanceof ServerLevel serverLevel) {
                Server.instance().broadcast(serverLevel, worldPosition, new ServerUpdateChopsPacket(worldPosition, getUpdateTag()));
            }
        }

        protected void rerender() {
            if (level != null && level.isClientSide()) {
                Minecraft.getInstance().levelRenderer.setBlockDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
                Client.treeCache.invalidate(); // Always need to do this to update WAILAs when chops spill
            }
        }

        private void rerenderNeighborhood() {
            if (level != null && level.isClientSide()) {
                rerender();

                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (Direction dir : Direction.values()) {
                    pos.setWithOffset(worldPosition, dir);
                    if (level.getBlockEntity(pos) instanceof MyEntity neighbor) {
                        neighbor.rerender();
                    }
                }
            }
        }

        public Stream<Direction> streamSolidSides(BlockGetter level, BlockPos pos) {
            return ConfigHandler.removeBarkOnInteriorLogs.get()
                    ? Arrays.stream(Direction.values())
                    .filter(direction -> !shape.isSideOpen(direction))
                    .filter(direction -> {
                        BlockPos neighborPos = pos.relative(direction);
                        BlockState blockState = level.getBlockState(neighborPos);
                        return ChopUtil.isBlockChoppable(level, neighborPos, blockState) && blockState.isCollisionShapeFullBlock(level, neighborPos);
                    })
                    : Stream.empty();
        }

        public VoxelShape getOcclusionShape(BlockGetter level, BlockPos pos) {
            return Shapes.or(
                    Shapes.empty(),
                    streamSolidSides(level, pos)
                            .map(direction -> Shapes.create(FaceShape.get(direction).toAABB()))
                            .toArray(VoxelShape[]::new)
            );
        }
    }
}
