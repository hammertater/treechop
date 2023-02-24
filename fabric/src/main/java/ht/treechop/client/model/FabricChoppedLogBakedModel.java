package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBakedModel {
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer != null) {
            RenderMaterial shaded = renderer.materialById(RenderMaterial.MATERIAL_STANDARD);
            if (shaded != null) {
                emitBlockQuads(level, state, pos, randomSupplier, quad -> {
                    emitter.fromVanilla(quad, shaded, null);
                    emitter.emit();
                });
            }
        }
    }

    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Consumer<BakedQuad> emitter) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
            Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
            getQuads(
                    strippedState,
                    entity.getShape(),
                    entity.getRadius(),
                    randomSupplier.get(),
                    strippedNeighbors
            )
                    .forEach(emitter);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }
}
