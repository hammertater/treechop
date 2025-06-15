package ht.treechop.common.util;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AxeAccessor extends AxeItem {

    public AxeAccessor(ToolMaterial $$0, float $$1, float $$2, Properties $$3) {
        super($$0, $$1, $$2, $$3);
    }

    public static boolean isStrippable(Block block) {
        return STRIPPABLES.containsKey(block);
    }

    public static boolean isStripped(Block block) {
        return STRIPPABLES.containsValue(block);
    }

    public static BlockState getStripped(BlockState blockState) {
        Block stripped = STRIPPABLES.get(blockState.getBlock());
        return (stripped == null) ? null : BlockUtil.copyStateProperties(stripped.defaultBlockState(), blockState);
    }

}
