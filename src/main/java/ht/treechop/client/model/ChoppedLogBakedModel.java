package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.FaceShape;
import javafx.geometry.BoundingBox;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChoppedLogBakedModel implements IDynamicBakedModel {

    public static ModelProperty<ChoppedLogShape> SHAPE = new ModelProperty<>();
    public static ModelProperty<Integer> CHOPS = new ModelProperty<>();
    public static ModelProperty<Set<Direction>> SOLID_SIDES = new ModelProperty<>();
    private final IBakedModel staticModel;

    public ChoppedLogBakedModel(IBakedModel staticModel) {
        this.staticModel = staticModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        final ResourceLocation textureRL = new ResourceLocation("treechop:block/chopped_log");
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                .getSprite(textureRL);

        if (extraData.hasProperty(SHAPE) && extraData.hasProperty(CHOPS)) {
            if (side == null) {
                ChoppedLogShape shape = extraData.getData(SHAPE);
                int chops = extraData.getData(CHOPS);
                Set<Direction> solidSides = extraData.getData(SOLID_SIDES);

                BoundingBox box = shape.getBoundingBox(chops);

                float downY = (float) box.getMinY();
                float upY = (float) box.getMaxY();
                float northZ = (float) box.getMinZ();
                float southZ = (float) box.getMaxZ();
                float westX = (float) box.getMinX();
                float eastX = (float) box.getMaxX();

                Vector3f topNorthEast = new Vector3f(eastX, upY, northZ);
                Vector3f topNorthWest = new Vector3f(westX, upY, northZ);
                Vector3f topSouthEast = new Vector3f(eastX, upY, southZ);
                Vector3f topSouthWest = new Vector3f(westX, upY, southZ);
                Vector3f bottomNorthEast = new Vector3f(eastX, downY, northZ);
                Vector3f bottomNorthWest = new Vector3f(westX, downY, northZ);
                Vector3f bottomSouthEast = new Vector3f(eastX, downY, southZ);
                Vector3f bottomSouthWest = new Vector3f(westX, downY, southZ);

                return Stream.concat(
                        Stream.of(
                            makeQuad(textureRL, sprite, bottomSouthEast, bottomNorthWest, Direction.DOWN, null),
                            makeQuad(textureRL, sprite, topSouthEast, topNorthWest, Direction.UP, null),
                            makeQuad(textureRL, sprite, topNorthEast, bottomNorthWest, Direction.NORTH, null),
                            makeQuad(textureRL, sprite, topSouthEast, bottomSouthWest, Direction.SOUTH, null),
                            makeQuad(textureRL, sprite, topSouthWest, bottomNorthWest, Direction.WEST, null),
                            makeQuad(textureRL, sprite, topSouthEast, bottomNorthEast, Direction.EAST, null)
                        ),
                        solidSides.stream().map(
                                direction -> makeQuad(textureRL, sprite, FaceShape.get(direction), direction.getOpposite(), null)
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

    private BakedQuad makeQuad(ResourceLocation textureRL, TextureAtlasSprite sprite, FaceShape faceShape, Direction orientation, Direction culling) {
        return makeQuad(
                textureRL,
                sprite,
                faceShape.getCorner1(),
                faceShape.getCorner3(),
                orientation,
                culling
        );
    }

    public BakedQuad makeQuad(ResourceLocation textureRL, TextureAtlasSprite sprite, Vector3f posFrom, Vector3f posTo, Direction orientation, Direction culling) {
        return makeQuad(
                textureRL,
                sprite,
                posFrom,
                posTo,
                orientation,
                culling,
                getUVsForQuad(posFrom, posTo, orientation),
                0
        );
    }

    private float[] getUVsForQuad(Vector3f posFrom, Vector3f posTo, Direction orientation) {
        switch (orientation) {
            case UP:
            case DOWN:
                return new float[]{posFrom.getX(), posFrom.getZ(), posTo.getX(), posTo.getZ()};
            case EAST:
            case WEST:
                return new float[]{posFrom.getZ(), posFrom.getY(), posTo.getZ(), posTo.getY()};
            case NORTH:
            case SOUTH:
            default:
                return new float[]{posFrom.getX(), posFrom.getY(), posTo.getX(), posTo.getY()};
        }
    }

    public BakedQuad makeQuad(ResourceLocation textureRL, TextureAtlasSprite sprite, Vector3f posFrom, Vector3f posTo, Direction orientation, Direction culling, float[] uvs, int uvRotation) {
        return new FaceBakery().bakeQuad(
                posFrom,
                posTo,
                new BlockPartFace(culling, -1, textureRL.toString(), new BlockFaceUV(uvs, uvRotation)),
                sprite,
                orientation,
                SimpleModelTransform.IDENTITY,
                null,
                true,
                null
        );
    }

    @Override
    @Nonnull
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
    {
        if (!state.hasProperty(BlockStateProperties.CHOPPED_LOG_SHAPE) || !state.hasProperty(BlockStateProperties.CHOP_COUNT)) {
            throw new IllegalArgumentException(
                    String.format("Could not bake chopped log model; block state %s is missing \"%s\" or \"%s\"",
                            state.toString(),
                            BlockStateProperties.CHOPPED_LOG_SHAPE.getName(),
                            BlockStateProperties.CHOP_COUNT.getName()
                    )
            );
        }

        Set<Direction> solidSides = Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis().isHorizontal())
                .filter(direction -> {
                    BlockState blockState = world.getBlockState(pos.offset(direction));
                    return blockState.isSolid() && !(blockState.getBlock() instanceof ChoppedLogBlock);
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Direction.class)));

        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(SHAPE, state.get(BlockStateProperties.CHOPPED_LOG_SHAPE));
        builder.withInitial(CHOPS, state.get(BlockStateProperties.CHOP_COUNT));
        builder.withInitial(SOLID_SIDES, solidSides);
        return builder.build();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return staticModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return staticModel.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return staticModel.isSideLit();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return staticModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return staticModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return staticModel.getOverrides();
    }

}
