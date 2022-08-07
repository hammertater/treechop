package ht.treechop.common.config;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements StringRepresentable {
    LINEAR(
            numBlocks -> {
                double x = (double) numBlocks;
                double m = ConfigHandler.get().getLinearM();
                double b = ConfigHandler.get().getLinearB();
                return m * x + b;
            }
    ),
    LOGARITHMIC(
            numBlocks -> {
                double x = (double) numBlocks;
                double a = ConfigHandler.get().getLogarithmicA();
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
            Rounder rounder = ConfigHandler.get().getChopCountRounding();
            int unboundedCount = Math.max(1, rounder.round(preciseCalculation.apply(numBlocks)));
            return ConfigHandler.get().canRequireMoreChopsThanBlocks()
                    ? unboundedCount
                    : Math.min(numBlocks, unboundedCount);
        }
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
