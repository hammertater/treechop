package ht.treechop.api;

import ht.treechop.common.chop.Chop;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Deprecated
public interface TreeDataImmutable {

    Optional<Set<BlockPos>> getLogBlocks();

    @Deprecated
    Set<BlockPos> getLogBlocksOrEmpty();

    int getChops();

    Stream<BlockPos> streamLogs();

    Stream<BlockPos> streamLeaves();

    boolean hasLeaves();

    boolean isAProperTree(boolean mustHaveLeaves);

    boolean readyToFell(int numChops);

    /**
     * Use with caution, as this requires mapping out the whole tree. Use {@link TreeDataImmutable#readyToFell(int)} instead whenever possible to avoid unnecessary computations.
     */
    int numChopsNeededToFell();

    Collection<Chop> chop(BlockPos target, int numChops);

    void forEachLeaves(Consumer<BlockPos> consumer);
}
