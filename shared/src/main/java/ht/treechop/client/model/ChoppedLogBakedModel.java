package ht.treechop.client.model;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.FaceShape;
import ht.treechop.common.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel {
    protected final ResourceLocation defaultTextureRL = new ResourceLocation(TreeChop.MOD_ID, "block/chopped_log");

    protected TextureAtlasSprite getSpriteForBlockSide(BlockState blockState, Direction side, RandomSource rand) {
        ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(blockState);
        return Minecraft.getInstance().getModelManager().getModel(modelLocation)
                .getQuads(blockState, side, rand).stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(BakedQuad::getSprite)
                .orElse(getDefaultSprite());
    }

    protected abstract TextureAtlasSprite getDefaultSprite();

    protected Stream<BakedQuad> getQuads(BlockState strippedState, ChoppedLogShape shape, int chops, Set<Direction> solidSides, RandomSource random) {
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
                                getSpriteForBlockSide(strippedState, direction.getOpposite(), random),
                                FaceShape.get(direction),
                                direction.getOpposite(),
                                null
                        )
                )
        ).filter(Objects::nonNull);
    }
}
