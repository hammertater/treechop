package ht.treechop.common.config;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements StringRepresentable {
    LINEAR(
            numBlocks -> {
                double x = (double) numBlocks;
                double m = ConfigHandler.COMMON.linearM.get();
                double b = ConfigHandler.COMMON.linearB.get();
                return m * x + b;
            },
            numChops -> {
                double y = (double) numChops;
                double m = ConfigHandler.COMMON.linearM.get();
                double b = ConfigHandler.COMMON.linearB.get();
                return (y - b) / m;
            }),
    LOGARITHMIC(
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.COMMON.logarithmicA.get();
                return 1 + a * log(1 + (x - 1) / a);
            },
            numChops -> {
                double y = (double) numChops;
                double a = ConfigHandler.COMMON.logarithmicA.get();
                return 1 + a * (exp((y - 1) / a) - 1);
            });

    private final Function<Integer, Double> forwardCalculation;
    private final Function<Integer, Double> inverseCalculation;

    ChopCountingAlgorithm(Function<Integer, Double> forwardCalculation, Function<Integer, Double> inverseCalculation) {
        this.forwardCalculation = forwardCalculation;
        this.inverseCalculation = inverseCalculation;
    }

    public int numChopsToFell(int numBlocks) {
        // y = floor(f(x))
        // x+ = max_x (f(x) < y + 1)
        if (numBlocks == 1) {
            return 1;
        } else {
            Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
            int unboundedCount = Math.max(1, rounder.round(forwardCalculation.apply(numBlocks)));
            return ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get()
                    ? unboundedCount
                    : Math.min(numBlocks, unboundedCount);
        }
    }

    public double inverse(int numChops) {
        return inverseCalculation.apply(numChops);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
