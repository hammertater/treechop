package ht.treechop.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class HiddenChoppedLogBakedModel extends ChoppedLogBakedModel {
    private final List<BakedQuad> quads;

    public HiddenChoppedLogBakedModel() {
        this(Collections.emptyList());
    }

    public HiddenChoppedLogBakedModel(List<BakedQuad> quads) {
        this.quads = quads;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, @NotNull RandomSource random) {
        return quads;
    }
}
