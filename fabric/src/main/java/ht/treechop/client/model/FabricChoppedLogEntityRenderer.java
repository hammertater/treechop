package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ht.treechop.client.FabricClient;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity> {
    protected final TextureAtlasSprite defaultSprite;
    private final Random random = new Random();
    private final BlockRenderContext renderContext = new BlockRenderContext();

    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
    }

    @Override
    public void render(FabricChoppedLogBlock.MyEntity entity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        Level level = entity.getLevel();
        if (level != null) {
            BlockPos pos = entity.getBlockPos();
            BlockState blockState = entity.getLevel().getBlockState(pos);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.solid());
            renderContext.render(level, FabricClient.choppedLogModel, blockState, pos, poseStack, vertexConsumer, random, 0L, overlay);
        }
    }
}
