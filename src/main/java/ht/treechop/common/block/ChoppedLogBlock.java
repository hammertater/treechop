package ht.treechop.common.block;

import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static ht.treechop.common.util.ChopUtil.isBlockLeaves;

public class ChoppedLogBlock extends Block implements IChoppableBlock, EntityBlock {

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
        return Shapes.box(
                box.minX,
                box.minY,
                box.minZ,
                box.maxX,
                box.maxY,
                box.maxZ
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHOPS, SHAPE);
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
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        return new Entity(pos, blockState);
    }

    @Override
    public void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
        ChoppedLogShape shape = getPlacementShape(level, pos);

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
                BlockState newBlockState = (blockState.is(this)
                        ? blockState
                        : defaultBlockState().setValue(BlockStateProperties.CHOPPED_LOG_SHAPE, shape))
                        .setValue(CHOPS, newNumChops);
                if (level.setBlock(pos, newBlockState, 3)) {
                    if (!blockState.is(this) && level.getBlockEntity(pos) instanceof Entity entity && level instanceof ServerLevel serverLevel) {
                        List<ItemStack> drops = Block.getDrops(blockState, serverLevel, pos, entity, player, tool);
                        entity.setOriginalState(blockState);
                        entity.setDrops(drops);
                    }
                }
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public void onRemove(BlockState blockState1, Level level, BlockPos pos, BlockState blockState2, boolean flag) {
        if (!blockState1.is(blockState2.getBlock())) {
            if (level.getBlockEntity(pos) instanceof Entity entity) {
                entity.drops.forEach(stack -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack));
            }

            super.onRemove(blockState1, level, pos, blockState2, flag);
        }
    }

    public static class Entity extends BlockEntity {

        private BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
        private List<ItemStack> drops = Collections.emptyList();

        public Entity(BlockPos pos, BlockState blockState) {
            super(ModBlocks.CHOPPED_LOG_ENTITY.get(), pos, blockState);
        }

        public void setOriginalState(BlockState originalState) {
            this.originalState = originalState;
        }

        public void setDrops(List<ItemStack> drops) {
            this.drops = drops;
        }

        public List<ItemStack> getDrops() {
            return drops;
        }

        public BlockState getOriginalState() {
            return originalState;
        }

        @Nonnull
        @Override
        public CompoundTag save(@Nonnull CompoundTag tag)
        {
            super.save(tag);

            ListTag list = new ListTag();
            drops.stream().map(stack -> stack.save(new CompoundTag()))
                    .forEach(list::add);
            tag.put("Drops", list);

            return tag;
        }

        @Override
        public void load(@Nonnull CompoundTag tag)
        {
            super.load(tag);

            ListTag list = tag.getList("Drops", 10);

            drops = new ArrayList<>(list.size());
            for(int i = 0; i < list.size(); ++i) {
                CompoundTag item = list.getCompound(i);
                drops.set(i, ItemStack.of(item));
            }
        }
    }
}
