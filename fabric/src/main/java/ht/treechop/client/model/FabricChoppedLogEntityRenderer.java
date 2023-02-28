package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.Random;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity> {
    protected final TextureAtlasSprite defaultSprite;
    private final Random random = new Random();
    private final BlockRenderDispatcher blockRenderer;

    @SuppressWarnings("deprecation")
    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(DEFAULT_TEXTURE_RESOURCE);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(FabricChoppedLogBlock.MyEntity entity, float somethingGood, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int light, int overlay) {
        Level level = entity.getLevel();
        if (level != null) {
            BlockPos pos = entity.getBlockPos();

            this.blockRenderer.getModelRenderer().tesselateBlock(level,
                    getQuadsAsBakedModel(level, entity.getOriginalState(), pos, () -> random),
                    entity.getBlockState(),
                    pos,
                    poseStack,
                    multiBufferSource.getBuffer(RENDER_TYPE),
                    true,
                    random,
                    0L,
                    overlay);
        }
    }

    private BakedModel getQuadsAsBakedModel(Level level, BlockState originalState, BlockPos pos, Supplier<Random> random) {
        List<BakedQuad> quads = new LinkedList<>();
        emitBlockQuads(level, originalState, pos, random, quads::add);
        return new HiddenChoppedLogBakedModel(quads);
    }

}
