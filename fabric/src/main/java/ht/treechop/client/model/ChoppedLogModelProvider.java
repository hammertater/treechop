package ht.treechop.client.model;

import ht.treechop.TreeChop;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelProvider implements ModelResourceProvider {
    private final UnbakedModel model;
    private static final ResourceLocation CHOPPED_LOG = TreeChop.resource("block/chopped_log");

    public ChoppedLogModelProvider(UnbakedModel model) {
        this.model = model;
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context) throws ModelProviderException {
        if (resourceId.equals(CHOPPED_LOG)) {
            return model;
        } else {
            return null;
        }
    }
}
