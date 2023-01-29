package ht.treechop.client.model;

import com.mojang.datafixers.util.Pair;
import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ForgeChoppedLogBakedModel extends ChoppedLogBakedModel implements IDynamicBakedModel {
    public static ModelProperty<Map<Direction, BlockState>> STRIPPED_NEIGHBORS = new ModelProperty<>();
    public static ModelProperty<BlockState> STRIPPED_BLOCK_STATE = new ModelProperty<>();
    public static ModelProperty<Integer> RADIUS = new ModelProperty<>();
    public static ModelProperty<ChoppedLogShape> CHOPPED_LOG_SHAPE = new ModelProperty<>();

    public static void overrideBlockStateModels(ModelBakeEvent event) {
        for (BlockState blockState : ForgeModBlocks.CHOPPED_LOG.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
            BakedModel existingModel = event.getModelManager().getModel(variantMRL);
            if (existingModel == event.getModelManager().getMissingModel()) {
                TreeChop.LOGGER.warn("Did not find the expected vanilla baked model(s) for treechop:chopped_log in registry");
            } else if (existingModel instanceof ForgeChoppedLogBakedModel) {
                TreeChop.LOGGER.warn("Tried to replace ChoppedLogBakedModel twice");
            } else {
                BakedModel customModel = new ForgeChoppedLogBakedModel().bake(event.getModelLoader(), ForgeModelBakery.defaultTextureGetter(), null, null);
                event.getModelRegistry().put(variantMRL, customModel);
            }
        }
    }

    @Override
    public List<Pair<BakedModel, RenderType>> getLayerModels(ItemStack itemStack, boolean fabulous) {
        return Collections.singletonList(Pair.of(this, RenderType.translucent()));
    }

    @Override
    @Nonnull
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());

            ModelDataMap.Builder builder = new ModelDataMap.Builder();
            builder.withInitial(STRIPPED_NEIGHBORS, getStrippedNeighbors(level, pos, entity));
            builder.withInitial(STRIPPED_BLOCK_STATE, strippedState);
            builder.withInitial(RADIUS, entity.getRadius());
            builder.withInitial(CHOPPED_LOG_SHAPE, entity.getShape());

            return builder.build();
        } else {
            return new ModelDataMap.Builder().build();
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        if (side == null) {
            BlockState strippedState = (extraData.hasProperty(STRIPPED_BLOCK_STATE))
                    ? extraData.getData(STRIPPED_BLOCK_STATE)
                    : Blocks.STRIPPED_OAK_LOG.defaultBlockState();

            Map<Direction, BlockState> strippedNeighbors = or(extraData.getData(STRIPPED_NEIGHBORS), Collections.emptyMap());
            ChoppedLogShape shape = or(extraData.getData(CHOPPED_LOG_SHAPE), ChoppedLogShape.PILLAR_Y);
            int radius = or(extraData.getData(RADIUS), 8);

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

    private <T> T or(T value, T fallback) {
        return value != null ? value : fallback;
    }
}