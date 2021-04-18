package ht.treechop.common.config;

import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements IStringSerializable {
    LINEAR(
            numBlocks -> {
                double x = (double) numBlocks;
                double m = ConfigHandler.linearM;
                double b = ConfigHandler.linearB;
                return m * x + b;
            }
    ),
    LOGARITHMIC(
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.logarithmicA;
                return 1 + a * log(1 + (x - 1) / a);
            }
    );

    private final Function<Integer, Double> preciseCalculation;

    ChopCountingAlgorithm(Function<Integer, Double> preciseCalculation) {
        this.preciseCalculation = preciseCalculation;
    }

    public int calculate(int numBlocks) {
        if (numBlocks == 1) {
            return 1;
        } else {
            Rounder rounder = ConfigHandler.chopCountRounding;
            int unboundedCount = Math.max(1, rounder.round(preciseCalculation.apply(numBlocks)));
            return ConfigHandler.canRequireMoreChopsThanBlocks
                    ? unboundedCount
                    : Math.min(numBlocks, unboundedCount);
        }
    }

    @Override
    public String getName() {
        return name();
    }
}
