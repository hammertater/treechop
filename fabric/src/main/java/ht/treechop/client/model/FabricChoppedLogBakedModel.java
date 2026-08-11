package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
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
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBakedModel {
    private static final RenderMaterial STANDARD_MATERIAL = Renderer.get().materialFinder().shadeMode(ShadeMode.VANILLA).find();

    @Override
    public boolean isVanillaAdapter() {
        return false;
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
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest) {
        Consumer<BakedQuad> bakedQuadEmitter = bakedQuad -> {
            emitter.fromVanilla(bakedQuad, STANDARD_MATERIAL, bakedQuad.getDirection());
            emitter.emit();
        };
        this.emitBlockQuads(level, state, pos, randomSupplier.get(), bakedQuadEmitter);
    }
}
