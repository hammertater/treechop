package ht.treechop.client.model;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.properties.ModBlockStateProperties;
import ht.treechop.common.util.FaceShape;
import ht.treechop.common.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.*;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChoppedLogBakedModel implements IDynamicBakedModel {

    public static ModelProperty<Set<Direction>> SOLID_SIDES = new ModelProperty<>();
    public static ModelProperty<BlockState> STRIPPED_BLOCK_STATE = new ModelProperty<>();
    private final BakedModel staticModel;
    private final ResourceLocation defaultTextureRL = new ResourceLocation("treechop:block/chopped_log");
    private final TextureAtlasSprite defaultSprite;
    private final boolean removeBarkOnInteriorLogs;

    public ChoppedLogBakedModel(BakedModel staticModel, boolean removeBarkOnInteriorLogs) {
        this.staticModel = staticModel;
        this.removeBarkOnInteriorLogs = removeBarkOnInteriorLogs;
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
    }

    public static void overrideBlockStateModels(ModelBakeEvent event) {
        for (BlockState blockState : ModBlocks.CHOPPED_LOG.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
            BakedModel existingModel = event.getModelRegistry().get(variantMRL);
            if (existingModel == null) {
                TreeChopMod.LOGGER.warn("Did not find the expected vanilla baked model(s) for treechop:chopped_log in registry");
            } else if (existingModel instanceof ChoppedLogBakedModel) {
                TreeChopMod.LOGGER.warn("Tried to replace ChoppedLogBakedModel twice");
            } else {
                ChoppedLogBakedModel customModel = new ChoppedLogBakedModel(
                        existingModel,
                        ConfigHandler.CLIENT.removeBarkOnInteriorLogs.get()
                );
                event.getModelRegistry().put(variantMRL, customModel);
            }
        }
    }

    @Override
    @Nonnull
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        if (!state.hasProperty(ModBlockStateProperties.CHOPPED_LOG_SHAPE) || !state.hasProperty(ModBlockStateProperties.CHOP_COUNT)) {
            throw new IllegalArgumentException(
                    String.format("Could not bake chopped log model; block state %s is missing \"%s\" or \"%s\"",
                            state.toString(),
                            ModBlockStateProperties.CHOPPED_LOG_SHAPE.getName(),
                            ModBlockStateProperties.CHOP_COUNT.getName()
                    )
            );
        }

        ChoppedLogShape shape = state.getValue(ModBlockStateProperties.CHOPPED_LOG_SHAPE);
        Set<Direction> solidSides = removeBarkOnInteriorLogs
                ? Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis().isHorizontal() && !shape.isSideOpen(direction))
                .filter(direction -> {
                    BlockState blockState = level.getBlockState(pos.relative(direction));
                    Block block = blockState.getBlock();
                    return blockState.isSolidRender(level, pos) && !(block instanceof ChoppedLogBlock);
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Direction.class)))
                : Collections.emptySet();

        BlockState strippedState;
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.Entity entity) {
            strippedState = entity.getStrippedOriginalState();
        } else {
            strippedState = Blocks.OAK_LOG.defaultBlockState();
        }

        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(SOLID_SIDES, solidSides);
        builder.withInitial(STRIPPED_BLOCK_STATE, strippedState);
        return builder.build();
    }

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull Random rand,
            @Nonnull IModelData extraData
    ) {
        if (side == null) {
            BlockState strippedState = (extraData.hasProperty(STRIPPED_BLOCK_STATE))
                    ? extraData.getData(STRIPPED_BLOCK_STATE)
                    : Blocks.STRIPPED_OAK_LOG.defaultBlockState();

            Set<Direction> solidSides = (extraData.hasProperty(SOLID_SIDES))
                    ? extraData.getData(SOLID_SIDES)
                    : Collections.emptySet();

            AABB box;
            if (state != null) {
                int chops = state.getValue(ModBlockStateProperties.CHOP_COUNT);
                ChoppedLogShape shape = state.getValue(ModBlockStateProperties.CHOPPED_LOG_SHAPE);
                box = shape.getBoundingBox(chops);
            } else {
                box = Shapes.box(0, 0, 0, 16, 16, 16).bounds();
            }

            float downY = (float) box.minY;
            float upY = (float) box.maxY;
            float northZ = (float) box.minZ;
            float southZ = (float) box.maxZ;
            float westX = (float) box.minX;
            float eastX = (float) box.maxX;

            Vector3 topNorthEast = new Vector3(eastX, upY, northZ);
            Vector3 topNorthWest = new Vector3(westX, upY, northZ);
            Vector3 topSouthEast = new Vector3(eastX, upY, southZ);
            Vector3 topSouthWest = new Vector3(westX, upY, southZ);
            Vector3 bottomNorthEast = new Vector3(eastX, downY, northZ);
            Vector3 bottomNorthWest = new Vector3(westX, downY, northZ);
            Vector3 bottomSouthEast = new Vector3(eastX, downY, southZ);
            Vector3 bottomSouthWest = new Vector3(westX, downY, southZ);

            //noinspection SuspiciousNameCombination
            return Stream.concat(
                    Stream.of(
                            Triple.of(bottomSouthEast, bottomNorthWest, Direction.DOWN),
                            Triple.of(topSouthEast, topNorthWest, Direction.UP),
                            Triple.of(topNorthEast, bottomNorthWest, Direction.NORTH),
                            Triple.of(topSouthEast, bottomSouthWest, Direction.SOUTH),
                            Triple.of(topSouthWest, bottomNorthWest, Direction.WEST),
                            Triple.of(topSouthEast, bottomNorthEast, Direction.EAST)
                    ).map(
                            triple -> ModelUtil.makeQuad(
                                    getSpriteForBlockSide(strippedState, triple.getRight(), rand, extraData),
                                    triple.getLeft(),
                                    triple.getMiddle(),
                                    triple.getRight(),
                                    null
                            )
                    ),
                    solidSides.stream().map(
                            direction -> ModelUtil.makeQuad(
                                    getSpriteForBlockSide(strippedState, direction.getOpposite(), rand, extraData),
                                    FaceShape.get(direction),
                                    direction.getOpposite(),
                                    null
                            )
                    )
            ).filter(Objects::nonNull).collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }

    private TextureAtlasSprite getSpriteForBlockSide(BlockState blockState, Direction side, Random rand, IModelData extraData) {
        ResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(blockState);
        return Minecraft.getInstance().getModelManager().getModel(modelLocation)
                .getQuads(blockState, side, rand, EmptyModelData.INSTANCE).stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(BakedQuad::getSprite)
                .orElse(defaultSprite);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return staticModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return staticModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return staticModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return staticModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return staticModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}
