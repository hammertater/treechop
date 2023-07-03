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

    private static final int CACHE_SIZE = 1024;
    private static final int[] cache = new int[CACHE_SIZE];
    private static int numCached = 1;

    private final Function<Integer, Double> preciseCalculation;

    ChopCountingAlgorithm(Function<Integer, Double> preciseCalculation) {
        this.preciseCalculation = preciseCalculation;
    }

    public int calculate(int numBlocks) {
        if (numBlocks < CACHE_SIZE) {
            if (numCached < numBlocks) {
                Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
                boolean canRequireMoreChopsThanBlocks = ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get();
                for (; numCached <= numBlocks; ++numCached) {
                    cache[numCached] = recalculate(numCached, this, rounder, canRequireMoreChopsThanBlocks);
                }
            }
            return cache[numBlocks];
        } else {
            Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
            boolean canRequireMoreChopsThanBlocks = ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get();
            return recalculate(numBlocks, this, rounder, canRequireMoreChopsThanBlocks);
        }
    }

    private int recalculate(int numBlocks, ChopCountingAlgorithm algo, Rounder rounder, boolean canRequireMoreChopsThanBlocks) {
        int count = Math.max(1, rounder.round(algo.preciseCalculation.apply(numBlocks)));
        return canRequireMoreChopsThanBlocks ? count : Math.min(numBlocks, count);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
