package ht.treechop.client.model;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChoppedLogBakedModel implements IDynamicBakedModel {

    public static ModelProperty<ChoppedLogShape> SHAPE = new ModelProperty<>();
    public static ModelProperty<Integer> CHOPS = new ModelProperty<>();
    public static ModelProperty<Set<Direction>> SOLID_SIDES = new ModelProperty<>();
    private final BakedModel staticModel;
    private final ResourceLocation textureRL = new ResourceLocation("treechop:block/chopped_log");
    private final TextureAtlasSprite sprite;
    private final boolean removeBarkOnInteriorLogs;

    public ChoppedLogBakedModel(BakedModel staticModel, boolean removeBarkOnInteriorLogs) {
        this.staticModel = staticModel;
        this.removeBarkOnInteriorLogs = removeBarkOnInteriorLogs;
        this.sprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(textureRL);
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
        if (!state.hasProperty(BlockStateProperties.CHOPPED_LOG_SHAPE) || !state.hasProperty(BlockStateProperties.CHOP_COUNT)) {
            throw new IllegalArgumentException(
                    String.format("Could not bake chopped log model; block state %s is missing \"%s\" or \"%s\"",
                            state.toString(),
                            BlockStateProperties.CHOPPED_LOG_SHAPE.getName(),
                            BlockStateProperties.CHOP_COUNT.getName()
                    )
            );
        }

        ChoppedLogShape shape = state.getValue(BlockStateProperties.CHOPPED_LOG_SHAPE);
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

        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(SHAPE, state.getValue(BlockStateProperties.CHOPPED_LOG_SHAPE));
        builder.withInitial(CHOPS, state.getValue(BlockStateProperties.CHOP_COUNT));
        builder.withInitial(SOLID_SIDES, solidSides);
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
        if (extraData.hasProperty(SHAPE) && extraData.hasProperty(CHOPS)) {
            if (side == null) {
                ChoppedLogShape shape = extraData.getData(SHAPE);
                int chops = extraData.getData(CHOPS);
                Set<Direction> solidSides = extraData.getData(SOLID_SIDES);

                AABB box = shape.getBoundingBox(chops);

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

                return Stream.concat(
                        Stream.of(
                            ModelUtil.makeQuad(textureRL, sprite, bottomSouthEast, bottomNorthWest, Direction.DOWN, null),
                            ModelUtil.makeQuad(textureRL, sprite, topSouthEast, topNorthWest, Direction.UP, null),
                            ModelUtil.makeQuad(textureRL, sprite, topNorthEast, bottomNorthWest, Direction.NORTH, null),
                            ModelUtil.makeQuad(textureRL, sprite, topSouthEast, bottomSouthWest, Direction.SOUTH, null),
                            ModelUtil.makeQuad(textureRL, sprite, topSouthWest, bottomNorthWest, Direction.WEST, null),
                            ModelUtil.makeQuad(textureRL, sprite, topSouthEast, bottomNorthEast, Direction.EAST, null)
                        ),
                        solidSides.stream().map(
                                direction -> ModelUtil.makeQuad(textureRL, sprite, FaceShape.get(direction), direction.getOpposite(), null)
                        )
                ).filter(Objects::nonNull).collect(Collectors.toList());
            }
            else {
                return Collections.emptyList();
            }
        }
        else {
            return Collections.emptyList();
        }
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
