package ht.treechop.common.config;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.lang.Math.log;

public enum ChopCountingAlgorithm implements StringRepresentable {
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

    public int calculate(int numBlocks, Rounder rounder, boolean canRequireMoreChopsThanBlocks) {
        int count = Math.max(1, rounder.round(preciseCalculation.apply(numBlocks)));
        return canRequireMoreChopsThanBlocks ? count : Math.min(numBlocks, count);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
