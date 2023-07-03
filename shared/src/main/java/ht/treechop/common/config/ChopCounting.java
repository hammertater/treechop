package ht.treechop.common.config;

public class ChopCounting {

    private static final int CACHE_SIZE = 1024;
    private static final int[] cache = new int[CACHE_SIZE];
    private static int numCached = 1;

    // Careful with early class loading
    private static final Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
    private static final boolean canRequireMoreChopsThanBlocks = ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get();
    private static final ChopCountingAlgorithm algorithm = ConfigHandler.COMMON.chopCountingAlgorithm.get();

    public static int calculate(int numBlocks) {
        if (numBlocks < CACHE_SIZE) {
            if (numCached < numBlocks) {
                for (; numCached <= numBlocks; ++numCached) {
                    cache[numCached] = recalculate(numCached);
                }
            }
            return cache[numBlocks];
        } else {
            return recalculate(numBlocks);
        }
    }

    private static int recalculate(int numBlocks) {
        int count = Math.max(1, rounder.round(algorithm.calculate(numBlocks, rounder, canRequireMoreChopsThanBlocks)));
        return canRequireMoreChopsThanBlocks ? count : Math.min(numBlocks, count);
    }
}
