package ht.treechop.client.model;

import ht.treechop.TreeChop;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

import static ht.treechop.client.model.ChoppedLogBakedModel.DEFAULT_TEXTURE_RESOURCE;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
    private static final ResourceLocation CHOPPED_LOG = TreeChop.resource("block/chopped_log");
    private final Supplier<ChoppedLogBakedModel> model;

    public ChoppedLogModelLoadingPlugin(Supplier<ChoppedLogBakedModel> model) {
        this.model = model;
    }

    @Override
    public void initialize(Context pluginContext) {
        pluginContext.modifyModelOnLoad().register((original, context) -> {
            final ResourceLocation id = context.id();
            if(CHOPPED_LOG.equals(id)) {
                return new UnbakedModel() {
                    @Override
                    public void resolveDependencies(Resolver resolver) {
                    }

                    @Override
                    public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
                        ChoppedLogBakedModel.defaultSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, DEFAULT_TEXTURE_RESOURCE));
                        return model.get();
                    }
                };
            } else {
                return original;
            }
        });
    }
}
