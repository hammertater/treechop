package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.tuber.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel implements UnbakedModel, BakedModel {
    private static TextureAtlasSprite defaultSprite;
    protected static final ResourceLocation DEFAULT_TEXTURE_RESOURCE = new ResourceLocation("block/stripped_oak_log");
    public static final RenderType RENDER_TYPE = RenderType.cutout(); // Don't use translucent, looks nuts with shaders

    private static BlockState getStrippedNeighbor(BlockAndTintGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return ChopUtil.getStrippedState(level, pos, level.getBlockState(neighborPos));
    }

    protected Map<Direction, BlockState> getStrippedNeighbors(BlockAndTintGetter level, BlockPos pos, ChoppedLogBlock.MyEntity entity) {
        if (entity.getOriginalState().isSolidRender(level, pos)) {
            return entity.getShape().getSolidSides(level, pos).stream().collect(Collectors.toMap(
                    side -> side,
                    side -> getStrippedNeighbor(level, pos, side)
            ));
        } else {
            return Collections.emptyMap();
        }
    }

    protected List<BakedQuad> getBlockQuads(BlockState blockState, Direction side, RandomSource rand) {
        BakedModel model = getBlockModel(blockState);
        return model.getQuads(blockState, side, rand);
    }

    @NotNull
    public static BakedModel getBlockModel(BlockState blockState) {
        ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(blockState);
        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    @Override
    public BakedModel bake(@NotNull ModelBaker modelBakery, Function<Material, TextureAtlasSprite> textureGetter, @NotNull ModelState modelState, @NotNull ResourceLocation modelId) {
        defaultSprite = textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, DEFAULT_TEXTURE_RESOURCE));
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

    protected Stream<BakedQuad> getQuads(BlockState strippedState, ChoppedLogShape shape, int radius, RandomSource random, Map<Direction, BlockState> strippedNeighbors) {
        final Direction[] allDirections = { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null };
        AABB box = shape.getBoundingBox(radius);
        Vector3 mins = new Vector3(box.minX, box.minY, box.minZ);
        Vector3 maxes = new Vector3(box.maxX, box.maxY, box.maxZ);

        return Stream.concat(
                Arrays.stream(allDirections)
                .flatMap(
                        side -> getBlockQuads(strippedState, side, random).stream()
                                .map(quad -> ModelUtil.trimQuad(quad, mins, maxes))
                ),
                strippedNeighbors.entrySet().stream().flatMap(
                        entry -> {
                            Direction side = entry.getKey();
                            BlockState strippedNeighbor = entry.getValue();

                            Vec3i normal = side.getNormal().multiply(16);
                            Vector3 transform = new Vector3(normal.getX(), normal.getY(), normal.getZ());

                            return getBlockModel(strippedNeighbor).getQuads(strippedNeighbor, side.getOpposite(), random).stream().map(quad -> ModelUtil.translateQuad(quad, transform));
                        }
                )
        ).filter(Objects::nonNull);
    }

}
