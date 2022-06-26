package ht.treechop.common.block;

import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.properties.ModBlockStateProperties;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static ht.treechop.common.util.ChopUtil.isBlockLeaves;

public class ChoppedLogBlock extends BlockImitator implements IChoppableBlock, IWaterLoggable {

    public static final IntegerProperty CHOPS = ModBlockStateProperties.CHOP_COUNT;
    public static final EnumProperty<ChoppedLogShape> SHAPE = ModBlockStateProperties.CHOPPED_LOG_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ChoppedLogBlock(Properties properties) {
        super(properties
                .dynamicShape()
                .isViewBlocking((blockState, world, pos) -> false));
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(CHOPS, 1)
                        .setValue(SHAPE, ChoppedLogShape.PILLAR_Y)
                        .setValue(WATERLOGGED, Boolean.FALSE)
        );
    }

    @Override
    public BlockState getImitatedBlockState(IBlockReader world, BlockPos pos) {
        TileEntity entity = world.getBlockEntity(pos);
        if (entity instanceof Entity) {
            return ((Entity) entity).getOriginalState();
        } else {
            return Blocks.OAK_LOG.defaultBlockState();
        }
    }

    public static ChoppedLogShape getPlacementShape(World world, BlockPos blockPos) {
        final byte DOWN     = 1;
        final byte UP       = 1 << 1;
        final byte NORTH    = 1 << 2;
        final byte SOUTH    = 1 << 3;
        final byte WEST     = 1 << 4;
        final byte EAST     = 1 << 5;

        byte openSides = (byte) (
                (isBlockOpen(world, blockPos.below()) ? DOWN : 0)
                        | (!isBlockALog(world, blockPos.above()) ? UP : 0)
                        | (!isBlockALog(world, blockPos.north()) ? NORTH : 0)
                        | (!isBlockALog(world, blockPos.south()) ? SOUTH : 0)
                        | (!isBlockALog(world, blockPos.west()) ? WEST : 0)
                        | (!isBlockALog(world, blockPos.east()) ? EAST : 0)
        );

        return ChoppedLogShape.forOpenSides(openSides);
    }

    private static boolean isBlockOpen(World world, BlockPos pos) {
        return (world.isEmptyBlock(pos) || isBlockLeaves(world, pos));
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        double scale = 1.0 / 16.0;
        int chops = state.getValue(CHOPS);
        AxisAlignedBB box = state.getValue(SHAPE).getBoundingBox(chops);
        return VoxelShapes.box(
                box.minX * scale,
                box.minY * scale,
                box.minZ * scale,
                box.maxX * scale,
                box.maxY * scale,
                box.maxZ * scale
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(SHAPE).getOcclusionShape();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CHOPS, SHAPE, WATERLOGGED);
    }

    @Override
    public int getNumChops(World world, BlockPos pos, BlockState blockState) {
        return (blockState.is(this)) ? blockState.getValue(CHOPS) : 0;
    }

    @Override
    public int getMaxNumChops(World world, BlockPos blockPos, BlockState blockState) {
        return 7;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new Entity();
    }

    @Override
    public void chop(PlayerEntity player, ItemStack tool, World world, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
        int currentNumChops = (blockState.is(this)) ? getNumChops(world, pos, blockState) : 0;
        int newNumChops = Math.min(currentNumChops + numChops, ChopUtil.getMaxNumChops(world, pos, blockState));
        int numAddedChops = newNumChops - currentNumChops;

        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            for (int i = 0; i < numAddedChops; ++i) {
                getDrops(defaultBlockState(), serverWorld, pos, null, player, tool)
                        .forEach(stack -> popResource(serverWorld, pos, stack));
            }
        }

        if (!felling) {
            if (numAddedChops > 0) {
                BlockState newBlockState = (blockState.is(this) ? blockState : getPlacementState(world, pos))
                        .setValue(CHOPS, newNumChops);

                if (world.setBlock(pos, newBlockState, 3)) {
                    if (!blockState.is(this) && world.getBlockEntity(pos) instanceof Entity && world instanceof ServerWorld) {
                        Entity entity = (Entity) world.getBlockEntity(pos);
                        ServerWorld serverWorld = (ServerWorld) world;
                        BlockState strippedBlockState = blockState.getToolModifiedState(
                                world,
                                pos,
                                FakePlayerFactory.getMinecraft(serverWorld),
                                Items.DIAMOND_AXE.getDefaultInstance(),
                                ToolType.AXE
                        );

                        if (strippedBlockState == null) {
                            if (AxeAccessor.isStrippedLog(blockState.getBlock())) {
                                strippedBlockState = blockState;
                            } else {
                                strippedBlockState = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
                            }
                        }

                        entity.setOriginalState(blockState, strippedBlockState);

                        List<ItemStack> drops = Block.getDrops(blockState, serverWorld, pos, entity, player, tool);
                        entity.setDrops(drops);
                        entity.setChanged();
                    }
                }
            } else {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getPlacementState(context.getLevel(), context.getClickedPos());
    }

    private BlockState getPlacementState(World world, BlockPos pos) {
        return defaultBlockState()
                .setValue(SHAPE, getPlacementShape(world, pos))
                .setValue(WATERLOGGED, shouldPlaceAsWaterlogged(world, pos));
    }

    private boolean shouldPlaceAsWaterlogged(World world, BlockPos pos) {
        final Direction[] waterSourceDirections = {
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST,
                Direction.UP
        };

        return Arrays.stream(waterSourceDirections)
                .filter(direction -> world.getFluidState(pos.offset(direction.getNormal())).isSource())
                .limit(2)
                .count() == 2;
    }

    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, side, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return !state.getValue(WATERLOGGED) && getImitatedBlockState(world, pos).propagatesSkylightDown(world, pos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder context) {
        TileEntity entity = context.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (ConfigHandler.COMMON.dropLootForChoppedBlocks.get() && entity instanceof Entity) {
            return ((Entity) entity).drops;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        return (float)Math.min(0.35, getImitatedBlockState(world, pos).getDestroyProgress(player, world, pos));
    }

    public static class Entity extends TileEntity {

        private BlockState originalState = Blocks.OAK_LOG.defaultBlockState();
        private BlockState strippedOriginalState = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        private List<ItemStack> drops = Collections.emptyList();

        public Entity() {
            super(ModBlocks.CHOPPED_LOG_ENTITY.get());
        }

        public void setOriginalState(BlockState originalState, BlockState strippedOriginalState) {
            this.originalState = originalState;
            this.strippedOriginalState = strippedOriginalState;
        }

        public void setDrops(List<ItemStack> drops) {
            this.drops = drops;
        }

        public BlockState getOriginalState() {
            return originalState;
        }

        public BlockState getStrippedOriginalState() {
            return strippedOriginalState;
        }

        @Override
        public CompoundNBT save(@Nonnull CompoundNBT tag) {
            super.save(tag);

            tag.putInt("OriginalState", Block.getId(getOriginalState()));
            tag.putInt("StrippedOriginalState", Block.getId(getStrippedOriginalState()));

            ListNBT list = new ListNBT();
            drops.stream().map(stack -> stack.save(new CompoundNBT()))
                    .forEach(list::add);
            tag.put("Drops", list);

            return tag;
        }

        @Override
        public void load(BlockState state, CompoundNBT tag) {
            super.load(state, tag);

            int stateId = tag.getInt("OriginalState");
            int strippedStateId = tag.getInt("StrippedOriginalState");
            setOriginalState(
                    stateId > 0 ? Block.stateById(stateId) : Blocks.OAK_LOG.defaultBlockState(),
                    strippedStateId > 0 ? Block.stateById(strippedStateId) : Blocks.STRIPPED_OAK_LOG.defaultBlockState()
            );

            ListNBT list = tag.getList("Drops", 10);

            drops = new LinkedList<>();
            for(int i = 0; i < list.size(); ++i) {
                CompoundNBT item = list.getCompound(i);
                drops.add(ItemStack.of(item));
            }
        }

        @Override
        public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
            BlockState state = Minecraft.getInstance().level.getBlockState(packet.getPos());
            load(state, packet.getTag());
        }

        @Nonnull
        @Override
        public CompoundNBT getUpdateTag() {
            return save(new CompoundNBT());
        }

        @Nullable
        @Override
        public SUpdateTileEntityPacket getUpdatePacket() {
            return new SUpdateTileEntityPacket(getBlockPos(), 0, getUpdateTag());
        }
    }

    private abstract static class AxeAccessor extends AxeItem {

        public AxeAccessor(IItemTier p_i48530_1_, float p_i48530_2_, float p_i48530_3_, Properties p_i48530_4_) {
            super(p_i48530_1_, p_i48530_2_, p_i48530_3_, p_i48530_4_);
        }

        public static boolean isStrippedLog(Block block) {
            return STRIPABLES.containsValue(block);
        }
    }
}
