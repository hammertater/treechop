package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBakedModel {
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity && RendererAccess.INSTANCE.hasRenderer()) {
            RenderMaterial shaded = RendererAccess.INSTANCE.getRenderer().materialById(RenderMaterial.MATERIAL_STANDARD);

            if (shaded != null) {
                BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
                Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
                QuadEmitter emitter = context.getEmitter();
                getQuads(
                        strippedState,
                        entity.getShape(),
                        entity.getRadius(),
                        randomSupplier.get(),
                        strippedNeighbors
                )
                        .forEach(quad -> {
                            emitter.fromVanilla(quad, shaded, Direction.DOWN);
                            emitter.emit();
                        });
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }
}
