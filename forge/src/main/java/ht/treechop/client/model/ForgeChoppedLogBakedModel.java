package ht.treechop.client.model;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForgeChoppedLogBakedModel extends ChoppedLogBakedModel implements IDynamicBakedModel {
    public static ModelProperty<Map<Direction, BlockState>> STRIPPED_NEIGHBORS = new ModelProperty<>();
    public static ModelProperty<BlockState> STRIPPED_BLOCK_STATE = new ModelProperty<>();
    public static ModelProperty<Integer> RADIUS = new ModelProperty<>();
    public static ModelProperty<ChoppedLogShape> CHOPPED_LOG_SHAPE = new ModelProperty<>();

    public static void overrideBlockStateModels(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation oakLog = BlockModelShaper.stateToModelLocation(Blocks.OAK_LOG.defaultBlockState());
        ForgeChoppedLogBakedModel.setDefaultSprite(event.getModels().get(oakLog).getParticleIcon());

        for (BlockState blockState : ForgeModBlocks.CHOPPED_LOG.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
            BakedModel existingModel = event.getModels().get(variantMRL);
            if (existingModel == null) {
                TreeChop.LOGGER.warn("Did not find the expected vanilla baked model(s) for treechop:chopped_log in registry");
            } else if (existingModel instanceof ForgeChoppedLogBakedModel) {
                TreeChop.LOGGER.warn("Tried to replace ChoppedLogBakedModel twice");
            } else {
                BakedModel customModel = new ForgeChoppedLogBakedModel();
                event.getModels().put(variantMRL, customModel);
            }
        }
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RENDER_TYPE);
    }

    @Nonnull
    public static ModelData getModelData(
            ChoppedLogBlock.MyEntity entity
    ) {
        BlockState strippedState = ChopUtil.getStrippedState(entity.getLevel(), entity.getBlockPos(), entity.getOriginalState());

        ModelData.Builder builder = ModelData.builder();
        builder.with(STRIPPED_NEIGHBORS, getStrippedNeighbors(entity.getLevel(), entity.getBlockPos(), entity));
        builder.with(STRIPPED_BLOCK_STATE, strippedState);
        builder.with(RADIUS, entity.getRadius());
        builder.with(CHOPPED_LOG_SHAPE, entity.getShape());

        return builder.build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (side == null) {
            BlockState strippedState = or(extraData.get(STRIPPED_BLOCK_STATE), Blocks.STRIPPED_OAK_LOG.defaultBlockState());

            Map<Direction, BlockState> strippedNeighbors = or(extraData.get(STRIPPED_NEIGHBORS), Collections.emptyMap());
            ChoppedLogShape shape = or(extraData.get(CHOPPED_LOG_SHAPE), ChoppedLogShape.PILLAR_Y);
            int radius = or(extraData.get(RADIUS), 8);

            return getQuads(strippedState,
                    shape,
                    radius,
                    rand,
                    strippedNeighbors
            ).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState strippedState = or(data.get(STRIPPED_BLOCK_STATE), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        return getBlockModel(strippedState).getParticleIcon();
    }

    private <T> T or(T value, T fallback) {
        return value != null ? value : fallback;
    }

}