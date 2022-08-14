package ht.treechop.common.util;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AxeAccessor extends AxeItem {
    public AxeAccessor(Tier p_40521_, float p_40522_, float p_40523_, Properties p_40524_) {
        super(p_40521_, p_40522_, p_40523_, p_40524_);
    }

    public static boolean isStrippable(Block block) {
        return STRIPPABLES.containsKey(block);
    }

    public static boolean isStripped(Block block) {
        return STRIPPABLES.containsValue(block);
    }

    public static Block getStripped(Block block) {
        return STRIPPABLES.get(block);
    }

    public static BlockState getStripped(BlockState blockState) {
        Block stripped = STRIPPABLES.get(blockState.getBlock());
        return (stripped == null) ? null : stripped.defaultBlockState();
    }
}
