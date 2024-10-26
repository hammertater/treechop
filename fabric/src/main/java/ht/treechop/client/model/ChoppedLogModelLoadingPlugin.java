package ht.treechop.client.model;

import ht.treechop.TreeChop;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
    private static final ResourceLocation CHOPPED_LOG = TreeChop.resource("block/chopped_log");
    private final Supplier<ChoppedLogBakedModel> model;

    public ChoppedLogModelLoadingPlugin(Supplier<ChoppedLogBakedModel> model) {
        this.model = model;
    }

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        pluginContext.modifyModelOnLoad().register((original, context) -> {
            final ResourceLocation id = context.resourceId();
            if(CHOPPED_LOG.equals(id)) {
                return model.get();
            } else {
                return original;
            }
        });
    }
}
