package ht.treechop.common.config;

import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

public enum Rounder implements IStringSerializable {
    DOWN(value -> (int) Math.floor(value)),
    NEAREST(value -> (int) Math.round(value)),
    UP(value -> (int) Math.ceil(value))
    ;

    private final Function<Double, Integer> transformation;

    Rounder(Function<Double, Integer> transformation) {
        this.transformation = transformation;
    }

    public int round(double value) {
        return transformation.apply(value);
    }

    @Override
    public String getName() {
        return name();
    }

}
