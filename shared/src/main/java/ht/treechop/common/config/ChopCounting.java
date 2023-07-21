package ht.treechop.common.config;

public class ChopCounting {

    private static final int CACHE_SIZE = 1024;
    private static final int[] cache = new int[CACHE_SIZE];
    private static int numCached = 1;

    // Careful with early class loading; TODO: reset and clear cache on config reload
    private static final Rounder rounder = ConfigHandler.COMMON.chopCountRounding.get();
    private static final boolean canRequireMoreChopsThanBlocks = ConfigHandler.COMMON.canRequireMoreChopsThanBlocks.get();
    private static final ChopCountingAlgorithm algorithm = ConfigHandler.COMMON.chopCountingAlgorithm.get();

    public static int calculate(int support) {
        if (support < CACHE_SIZE) {
            if (numCached <= support) {
                for (; numCached <= support; ++numCached) {
                    cache[numCached] = recalculate(numCached);
                }
            }
            return cache[support];
        } else {
            return recalculate(support);
        }
    }

    private static int recalculate(int support) {
        int count = Math.max(1, rounder.round(algorithm.calculate(support, rounder, canRequireMoreChopsThanBlocks)));
        return canRequireMoreChopsThanBlocks ? count : Math.min(support, count);
    }
}
