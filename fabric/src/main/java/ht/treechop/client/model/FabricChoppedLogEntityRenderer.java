package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ht.treechop.common.block.FabricChoppedLogBlock;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.Random;
import java.util.Set;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity> {
    protected final TextureAtlasSprite defaultSprite;
    private final Random random = new Random();

    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
    }

    @Override
    public void render(FabricChoppedLogBlock.MyEntity entity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int somethin1, int somethin2) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.solid());
        Set<Direction> solidSides = entity.getShape().getSolidSides(entity.getLevel(), entity.getBlockPos());

        final float RGB = 1f;
        getQuads(ChopUtil.getStrippedState(entity.getOriginalState()),
                entity.getShape(),
                entity.getChops(),
                solidSides,
                random)
                .forEach(quad -> vertexConsumer.putBulkData(poseStack.last(), quad, RGB, RGB, RGB, somethin1, somethin2));
    }
}
