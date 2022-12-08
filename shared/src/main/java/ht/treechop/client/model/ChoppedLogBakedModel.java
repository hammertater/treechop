package ht.treechop.client.model;

import com.mojang.datafixers.util.Pair;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.FaceShape;
import ht.treechop.common.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel implements UnbakedModel, BakedModel {
    private static TextureAtlasSprite defaultSprite;
    protected final ResourceLocation defaultTextureRL = new ResourceLocation("block/stripped_oak_log");

    protected TextureAtlasSprite getSpriteForBlockSide(BlockState blockState, Direction side, RandomSource rand) {
        ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(blockState);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);

        //noinspection ConstantConditions
        return getSpriteForBlockSide(model, blockState, side, rand)
                .or(() -> getSpriteForBlockSide(model, blockState, null, rand))
                .or(() -> Optional.ofNullable(model.getParticleIcon()))
                .orElse(defaultSprite);
    }

    protected Optional<TextureAtlasSprite> getSpriteForBlockSide(BakedModel model, BlockState blockState, Direction side, RandomSource rand) {
        //noinspection ConstantConditions
        return model.getQuads(blockState, side, rand).stream()
                .map(BakedQuad::getSprite)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public @NotNull Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    public @NotNull Collection<Material> getMaterials(@NotNull Function<ResourceLocation, UnbakedModel> var1, @NotNull Set<Pair<String, String>> var2) {
        return Collections.emptyList();
    }

    @Nullable
    public BakedModel bake(@NotNull ModelBakery modelBakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation modelId) {
        defaultSprite = textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, defaultTextureRL));
        return this;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, @NotNull RandomSource randomSource) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return getDefaultSprite();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    protected TextureAtlasSprite getDefaultSprite() {
        return defaultSprite;
    }

    protected Stream<BakedQuad> getQuads(BlockState strippedState, ChoppedLogShape shape, int chops, Set<Direction> solidSides, RandomSource random, Function<Direction, BlockState> neighborGetter) {
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
                                getSpriteForBlockSide(strippedState, triple.getRight(), random),
                                triple.getLeft(),
                                triple.getMiddle(),
                                triple.getRight(),
                                null
                        )
                ),
                solidSides.stream().map(
                        direction -> ModelUtil.makeQuad(
                                getSpriteForNeighbor(strippedState, neighborGetter, direction, random),
                                FaceShape.get(direction),
                                direction.getOpposite(),
                                null
                        )
                )
        ).filter(Objects::nonNull);
    }

    private TextureAtlasSprite getSpriteForNeighbor(BlockState blockState, Function<Direction, BlockState> neighborGetter, Direction direction, RandomSource random) {
        BlockState neighbor = neighborGetter.apply(direction);
        return getSpriteForBlockSide((neighbor != null) ? neighbor : blockState, direction.getOpposite(), random);
    }
}
