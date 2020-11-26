package ht.treechop.config;

import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

public enum ChopCountingAlgorithm implements IStringSerializable {
    LINEAR(
            "linear",
            numBlocks -> numBlocks
    ),
    LOGARITHMIC(
            "log",
            numBlocks -> (int) Math.floor(1 + 6 * log2(1 + ((double) numBlocks - 1) / 8))
    );

    private final String name;
    private final Function<Integer, Integer> calculation;

    ChopCountingAlgorithm(String name, Function<Integer, Integer> calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getString() {
        return name;
    }

    public int calculate(int numBlocks) {
        return calculation.apply(numBlocks);
    }

    static private double log2(double x) {
        final double invBase = 1 / (Math.log(2));
        return Math.log(x) * invBase;
    }
}
