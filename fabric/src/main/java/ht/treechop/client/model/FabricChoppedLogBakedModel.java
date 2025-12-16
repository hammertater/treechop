package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBakedModel {
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
