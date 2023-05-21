package ht.treechop.common.util;

import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.stream.Stream;

public class BlockNeighbors {

    protected final BlockPos[] blocks;

    static public final BlockNeighbors HORIZONTAL_ADJACENTS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors VERTICAL_ADJACENTS = new BlockNeighbors(Stream.of(
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors ADJACENTS = new BlockNeighbors(Stream.of(
            HORIZONTAL_ADJACENTS.stream(),
            VERTICAL_ADJACENTS.stream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL_DIAGONALS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL = new BlockNeighbors(Stream.of(
            HORIZONTAL_ADJACENTS.stream(),
            HORIZONTAL_DIAGONALS.stream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors ABOVE_ADJACENTS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, -1),
            new BlockPos(1, 1, 0),
            new BlockPos(0, 1, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors ABOVE_DIAGONALS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, 1, 1),
            new BlockPos(1, 1, -1),
            new BlockPos(1, 1, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors ABOVE = new BlockNeighbors(Stream.of(
            ABOVE_ADJACENTS.stream(),
            ABOVE_DIAGONALS.stream(),
            Stream.of(new BlockPos(0, 1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL_AND_ABOVE = new BlockNeighbors(Stream.of(
            HORIZONTAL.stream(),
            ABOVE.stream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors BELOW_ADJACENTS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, -1),
            new BlockPos(1, -1, 0),
            new BlockPos(0, -1, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors BELOW_DIAGONALS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, -1, -1),
            new BlockPos(-1, -1, 1),
            new BlockPos(1, -1, -1),
            new BlockPos(1, -1, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors BELOW = new BlockNeighbors(Stream.of(
            BELOW_ADJACENTS.stream(),
            BELOW_DIAGONALS.stream(),
            Stream.of(new BlockPos(0, -1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors ADJACENTS_AND_DIAGONALS = new BlockNeighbors(Stream.of(
            ABOVE.stream(),
            HORIZONTAL.stream(),
            BELOW.stream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors ADJACENTS_AND_BELOW_ADJACENTS = new BlockNeighbors(Stream.of(
            ADJACENTS.stream(),
            BELOW_ADJACENTS.stream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    public BlockNeighbors(BlockPos[] blocks) {
        this.blocks = blocks;
    }

    public Stream<BlockPos> stream() {
        return Arrays.stream(blocks);
    }

    public Stream<BlockPos> stream(BlockPos pos) {
        return Arrays.stream(blocks).map(pos::offset);
    }
}
