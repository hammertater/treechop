package ht.treechop.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class FabricChoppedLogEntityRenderer extends FabricChoppedLogBakedModel implements BlockEntityRenderer<FabricChoppedLogBlock.MyEntity> {
    protected final TextureAtlasSprite defaultSprite;
    private final RandomSource random = RandomSource.create();
    private final BlockRenderDispatcher blockRenderer;
    private List<BakedQuad> quads = Collections.emptyList();

    public FabricChoppedLogEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.defaultSprite = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(defaultTextureRL);
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
                    multiBufferSource.getBuffer(RenderType.solid()),
                    false,
                    random,
                    0L,
                    overlay);
        }
    }

    private BakedModel getQuadsAsBakedModel(Level level, BlockState originalState, BlockPos pos, Supplier<RandomSource> random) {
        List<BakedQuad> quads = new LinkedList<>();
        emitBlockQuads(level, originalState, pos, random, quads::add);
        return new HiddenChoppedLogBakedModel(quads);
    }

}
