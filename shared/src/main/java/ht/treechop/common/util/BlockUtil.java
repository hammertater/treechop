package ht.treechop.common.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockUtil {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BlockState copyStateProperties(BlockState blockState, BlockState stateToCopy) {
        for(Property property : stateToCopy.getProperties()) {
            if (blockState.hasProperty(property)) {
                blockState = blockState.setValue(property, stateToCopy.getValue(property));
            }
        }
        return blockState;
    }
}
