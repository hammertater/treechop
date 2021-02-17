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
                Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
                return rounder.round(m * x + b);
            }
    ),
    LOGARITHMIC(
            "log",
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.COMMON.logarithmicA.get();
                Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
                return rounder.round(1 + a * log(1 + (x - 1) / a));
            }
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

    @SuppressWarnings("NullableProblems")
    @Override
    public String getString() {
        return name;
    }

    public int calculate(int numBlocks) {
        return calculation.apply(numBlocks);
    }

}
