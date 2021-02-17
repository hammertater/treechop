package ht.treechop.common.config;

import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements IStringSerializable {
    LINEAR(
            "linear",
            numBlocks -> {
                double x = (double) numBlocks;
                double m = ConfigHandler.COMMON.linearM.get();
                double b = ConfigHandler.COMMON.linearB.get();
                return m * x + b;
            }
    ),
    LOGARITHMIC(
            "log",
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.COMMON.logarithmicA.get();
                return 1 + a * log(1 + (x - 1) / a);
            }
    );

    private final String name;
    private final Function<Integer, Double> calculation;

    ChopCountingAlgorithm(String name, Function<Integer, Double> calculation) {
        this.name = name;
        this.calculation = calculation;
    }

    public String toString() {
        return name;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getString() {
        return name;
    }

    public int calculate(int numBlocks) {
        Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
        int unboundedCount = Math.max(1, rounder.round(calculation.apply(numBlocks)));
        return ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get()
                ? unboundedCount
                : Math.min(numBlocks, unboundedCount);
    }

}
