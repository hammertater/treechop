package ht.treechop.common.util;

import net.minecraft.util.math.BlockPos;

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
            HORIZONTAL_ADJACENTS.asStream(),
            VERTICAL_ADJACENTS.asStream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL_DIAGONALS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL = new BlockNeighbors(Stream.of(
            HORIZONTAL_ADJACENTS.asStream(),
            HORIZONTAL_DIAGONALS.asStream()
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
            ABOVE_ADJACENTS.asStream(),
            ABOVE_DIAGONALS.asStream(),
            Stream.of(new BlockPos(0, 1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors HORIZONTAL_AND_ABOVE = new BlockNeighbors(Stream.of(
            HORIZONTAL.asStream(),
            ABOVE.asStream()
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
            BELOW_ADJACENTS.asStream(),
            BELOW_DIAGONALS.asStream(),
            Stream.of(new BlockPos(0, -1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors ADJACENTS_AND_DIAGONALS = new BlockNeighbors(Stream.of(
            ABOVE.asStream(),
            HORIZONTAL.asStream(),
            BELOW.asStream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    static public final BlockNeighbors ADJACENTS_AND_BELOW_ADJACENTS = new BlockNeighbors(Stream.of(
            ADJACENTS.asStream(),
            BELOW_ADJACENTS.asStream()
    ).flatMap(a -> a).toArray(BlockPos[]::new));

    public BlockNeighbors(BlockPos[] blocks) {
        this.blocks = blocks;
    }

    protected Stream<BlockPos> asStream() {
        return Arrays.stream(blocks);
    }

    public Stream<BlockPos> asStream(BlockPos pos) {
        return Arrays.stream(blocks).map(pos::offset);
    }
}
