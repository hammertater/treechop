package ht.treechop.client.model;

import com.mojang.datafixers.util.Pair;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.treechop.common.util.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements UnbakedModel, BakedModel, FabricBakedModel {

    private static TextureAtlasSprite defaultSprite;

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            Set<Direction> solidSides = entity.getShape().getSolidSides(level, pos);
            QuadEmitter emitter = context.getEmitter();
            getQuads(
                    ChopUtil.getStrippedState(entity.getOriginalState()),
                    entity.getShape(),
                    entity.getChops(),
                    solidSides,
                    randomSupplier.get()
            )
                    .forEach(quad -> {
                        emitter.fromVanilla(quad, IndigoRenderer.MATERIAL_STANDARD, quad.getDirection());
                        emitter.emit();
                    });
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation modelId) {
        defaultSprite = textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, defaultTextureRL));
        return this;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        // TODO: figure out how to use this properly
        //return staticModel.useAmbientOcclusion();
        return false;
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
    public TextureAtlasSprite getParticleIcon() {
        return defaultSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    protected TextureAtlasSprite getDefaultSprite() {
        return defaultSprite;
    }
}
