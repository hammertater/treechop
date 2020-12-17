package ht.treechop.common.properties;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;

public class BlockStateProperties {
    public static final PropertyInteger CHOP_COUNT = PropertyInteger.create("chops", 1, 7);
    public static final PropertyEnum<ChoppedLogShape> CHOPPED_LOG_SHAPE = PropertyEnum.create("shape", ChoppedLogShape.class);
}
