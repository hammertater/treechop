package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity> {
    protected final TextureAtlasSprite defaultSprite;
    private final RandomSource random = RandomSource.create();

    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
    }

    @Override
    public void render(FabricChoppedLogBlock.MyEntity entity, float somethingGood, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int light, int overlay) {
        Level level = entity.getLevel();
        if (level != null) {
            BlockPos pos = entity.getBlockPos();
            BlockState blockState = entity.getLevel().getBlockState(pos);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.solid());

            int color = Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, 0);
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), vertexConsumer, blockState, this, r, g, b, light, overlay);
        }
    }
}
