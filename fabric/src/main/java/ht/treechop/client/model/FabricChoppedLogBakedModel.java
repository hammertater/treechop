package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBlockStateModel {
    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        Renderer renderer = Renderer.get();
        if (renderer != null) {
            RenderMaterial shaded = renderer.materialById(RenderMaterial.STANDARD_ID);
            if (shaded != null) {
                emitBlockQuads(level, state, pos, random, quad -> {
                    emitter.fromVanilla(quad, shaded, null);
                    emitter.emit();
                });
            }
        }
    }

    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, RandomSource random, Consumer<BakedQuad> emitter) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
            Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
            getQuads(
                    strippedState,
                    entity.getShape(),
                    entity.getRadius(),
                    random,
                    strippedNeighbors
            )
                    .forEach(emitter);
        }
    }

    @Override
    public void resolveDependencies(Resolver resolver) {

    }
}
