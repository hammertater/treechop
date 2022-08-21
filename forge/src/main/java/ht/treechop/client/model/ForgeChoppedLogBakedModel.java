package ht.treechop.client.model;

import ht.treechop.TreeChop;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.properties.ModBlockStateProperties;
import ht.treechop.common.registry.ForgeModBlocks;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
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
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeChoppedLogBakedModel extends ChoppedLogBakedModel implements IDynamicBakedModel {
    protected final BakedModel staticModel;
    protected final TextureAtlasSprite defaultSprite;
    public static ModelProperty<Set<Direction>> SOLID_SIDES = new ModelProperty<>();
    public static ModelProperty<BlockState> STRIPPED_BLOCK_STATE = new ModelProperty<>();

    public ForgeChoppedLogBakedModel(BakedModel staticModel) {
        this.staticModel = staticModel;
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
    }

    public static void overrideBlockStateModels(ModelEvent.BakingCompleted event) {
        for (BlockState blockState : ForgeModBlocks.CHOPPED_LOG.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
            BakedModel existingModel = event.getModelManager().getModel(variantMRL);
            if (existingModel == event.getModelManager().getMissingModel()) {
                TreeChop.LOGGER.warn("Did not find the expected vanilla baked model(s) for treechop:chopped_log in registry");
            } else if (existingModel instanceof ForgeChoppedLogBakedModel) {
                TreeChop.LOGGER.warn("Tried to replace ChoppedLogBakedModel twice");
            } else {
                ForgeChoppedLogBakedModel customModel = new ForgeChoppedLogBakedModel(existingModel);
                event.getModels().put(variantMRL, customModel);
            }
        }
    }

    @Override
    @Nonnull
    public ModelData getModelData(
            @Nonnull BlockAndTintGetter level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull ModelData tileData
    ) {
        ModelData.Builder builder = ModelData.builder();
        builder.with(SOLID_SIDES, getSolidSides(level, pos, state));
        builder.with(STRIPPED_BLOCK_STATE, ChopUtil.getStrippedState(level, pos, state));
        return builder.build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (side == null && state != null) {
            BlockState strippedState = (extraData.has(STRIPPED_BLOCK_STATE))
                    ? extraData.get(STRIPPED_BLOCK_STATE)
                    : Blocks.STRIPPED_OAK_LOG.defaultBlockState();

            Set<Direction> solidSides = extraData.get(SOLID_SIDES);
            if (solidSides == null) {
                solidSides = Collections.emptySet();
            }

            int chops = state.hasProperty(ModBlockStateProperties.CHOP_COUNT) ? state.getValue(ModBlockStateProperties.CHOP_COUNT) : 0;
            ChoppedLogShape shape = state.getValue(ModBlockStateProperties.CHOPPED_LOG_SHAPE);

            return getQuads(strippedState, shape, chops, solidSides, rand).collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        // TODO: figure out how to use this properly
        //return staticModel.useAmbientOcclusion();
        return false;
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
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState strippedState = data.get(STRIPPED_BLOCK_STATE);
        if (strippedState != null) {
            return Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(strippedState)).getParticleIcon(data);
        } else {
            return getParticleIcon();
        }
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.solid());
    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }

    @Override
    protected TextureAtlasSprite getDefaultSprite() {
        return defaultSprite;
    }
}
