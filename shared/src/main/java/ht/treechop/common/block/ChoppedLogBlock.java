package ht.treechop.common.block;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.properties.ModBlockStateProperties;
import ht.treechop.common.util.AxeAccessor;
import ht.treechop.common.util.ChopUtil;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static ht.treechop.common.util.ChopUtil.isBlockLeaves;

public abstract class ChoppedLogBlock extends BlockImitator implements IChoppableBlock, EntityBlock, SimpleWaterloggedBlock {

    public static final IntegerProperty CHOPS = ModBlockStateProperties.CHOP_COUNT;
    public static final EnumProperty<ChoppedLogShape> SHAPE = ModBlockStateProperties.CHOPPED_LOG_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChoppedLogBlock(BlockBehaviour.Properties properties) {
        super(properties
                .dynamicShape()
                .isViewBlocking((BlockState blockState, BlockGetter level, BlockPos pos) -> false));
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(CHOPS, 1)
                        .setValue(SHAPE, ChoppedLogShape.PILLAR_Y)
                        .setValue(WATERLOGGED, Boolean.FALSE)
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

    public BlockState getImitatedBlockState(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MyEntity entity) {
            return entity.getOriginalState();
        } else {
            return Blocks.OAK_LOG.defaultBlockState();
        }
    }

    private static boolean isBlockOpen(Level level, BlockPos pos) {
        return (level.isEmptyBlock(pos) || isBlockLeaves(level, pos));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        double scale = 1.0 / 16.0;
        int chops = state.getValue(CHOPS);
        AABB box = state.getValue(SHAPE).getBoundingBox(chops);
        return Shapes.box(
                box.minX * scale,
                box.minY * scale,
                box.minZ * scale,
                box.maxX * scale,
                box.maxY * scale,
                box.maxZ * scale
        );
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHOPS, SHAPE, WATERLOGGED);
    }

    @Override
    public int getNumChops(Level level, BlockPos pos, BlockState blockState) {
        return (blockState.is(this)) ? blockState.getValue(CHOPS) : 0;
    }

    @Override
    public int getMaxNumChops(Level level, BlockPos blockPos, BlockState blockState) {
        return 7;
    }

    @Override
    public void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
        int currentNumChops = (blockState.is(this)) ? getNumChops(level, pos, blockState) : 0;
        int newNumChops = Math.min(currentNumChops + numChops, ChopUtil.getMaxNumChops(level, pos, blockState));
        int numAddedChops = newNumChops - currentNumChops;

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < numAddedChops; ++i) {
                getDrops(defaultBlockState(), serverLevel, pos, null, player, tool)
                        .forEach(stack -> popResource(serverLevel, pos, stack));
            }
        }

        if (!felling) {
            if (numAddedChops > 0) {
                BlockState newBlockState = (blockState.is(this) ? blockState : getPlacementState(level, pos))
                        .setValue(CHOPS, newNumChops);

                if (level.setBlockAndUpdate(pos, newBlockState)) {
                    if (!blockState.is(this) && level.getBlockEntity(pos) instanceof MyEntity entity && level instanceof ServerLevel serverLevel) {
                        BlockState strippedBlockState = AxeAccessor.getStripped(blockState);

                        if (strippedBlockState == null) {
                            if (AxeAccessor.isStripped(blockState.getBlock())) {
                                strippedBlockState = blockState;
                            } else {
                                strippedBlockState = ConfigHandler.inferredStrippedStates.get()
                                        .getOrDefault(blockState.getBlock(), blockState.getBlock().defaultBlockState());
                            }
                        }

                        entity.setOriginalState(blockState);

                        List<ItemStack> drops = Block.getDrops(blockState, serverLevel, pos, entity, player, tool);
                        entity.setDrops(drops);
                        entity.setChanged();
                    }
                }
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
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
                .setValue(SHAPE, getPlacementShape(level, pos))
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

    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    public BlockState updateShape(BlockState blockState, Direction side, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (blockState.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, side, neighborState, level, pos, neighborPos);
    }

    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter level, BlockPos pos) {
        return !blockState.getValue(WATERLOGGED) && super.propagatesSkylightDown(blockState, level, pos);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public List<ItemStack> getDrops(@Nonnull BlockState blockState, LootContext.Builder context) {
        if (ConfigHandler.COMMON.dropLootForChoppedBlocks.get() && context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MyEntity entity) {
            return entity.drops;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        return (float)Math.min(0.35, getImitatedBlockState(level, pos).getDestroyProgress(player, level, pos));
    }

    public static abstract class MyEntity extends BlockEntity {

        protected BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
        protected List<ItemStack> drops = Collections.emptyList();

        public MyEntity(BlockPos pos, BlockState blockState) {
            super(TreeChop.platform.getChoppedLogBlockEntity(), pos, blockState);
        }

        public void setOriginalState(BlockState originalState) {
            this.originalState = originalState;
        }

        public void setDrops(List<ItemStack> drops) {
            this.drops = drops;
        }

        public BlockState getOriginalState() {
            return originalState;
        }

        @Override
        public void saveAdditional(@Nonnull CompoundTag tag)
        {
            super.saveAdditional(tag);

            tag.putInt("OriginalState", Block.getId(getOriginalState()));

            ListTag list = new ListTag();
            drops.stream().map(stack -> stack.save(new CompoundTag()))
                    .forEach(list::add);
            tag.put("Drops", list);
        }

        @Override
        public void load(@Nonnull CompoundTag tag)
        {
            super.load(tag);

            int stateId = tag.getInt("OriginalState");
            setOriginalState(stateId > 0 ? Block.stateById(stateId) : Blocks.OAK_LOG.defaultBlockState());

            ListTag list = tag.getList("Drops", 10);

            drops = new LinkedList<>();
            for(int i = 0; i < list.size(); ++i) {
                CompoundTag item = list.getCompound(i);
                drops.add(ItemStack.of(item));
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
    }

}
