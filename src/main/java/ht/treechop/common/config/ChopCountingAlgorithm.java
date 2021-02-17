package ht.treechop.common.config;

import net.minecraft.util.IStringSerializable;

import java.util.function.Function;

import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements IStringSerializable {
    LINEAR(
            numBlocks -> {
                double x = (double) numBlocks;
                double m = ConfigHandler.COMMON.linearM.get();
                double b = ConfigHandler.COMMON.linearB.get();
                return m * x + b;
            }
    ),
    LOGARITHMIC(
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.COMMON.logarithmicA.get();
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
            Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
            int unboundedCount = Math.max(1, rounder.round(preciseCalculation.apply(numBlocks)));
            return ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get()
                    ? unboundedCount
                    : Math.min(numBlocks, unboundedCount);
        }
    }

    @Override
    public String getName() {
        return name();
    }
}
