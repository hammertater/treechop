package ht.treechop.common.properties;

import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;

public class ModBlockStateProperties {
    public static final IntegerProperty CHOP_COUNT = IntegerProperty.create("chops", 1, 7);
    public static final EnumProperty<ChoppedLogShape> CHOPPED_LOG_SHAPE = EnumProperty.create("shape", ChoppedLogShape.class);
}
