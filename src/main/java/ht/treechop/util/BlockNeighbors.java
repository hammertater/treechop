package ht.treechop.util;

import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.stream.Stream;

public class BlockNeighbors {

    static public final BlockPos[] HORIZONTAL_ADJACENTS = Stream.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] VERTICAL_ADJACENTS = Stream.of(
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ADJACENTS = Stream.of(
            Arrays.stream(HORIZONTAL_ADJACENTS),
            Arrays.stream(VERTICAL_ADJACENTS)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_DIAGONALS = Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL = Stream.of(
            Arrays.stream(HORIZONTAL_ADJACENTS),
            Arrays.stream(HORIZONTAL_DIAGONALS)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE_ADJACENTS = Stream.of(
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, -1),
            new BlockPos(1, 1, 0),
            new BlockPos(0, 1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE_DIAGONALS = Stream.of(
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, 1, 1),
            new BlockPos(1, 1, -1),
            new BlockPos(1, 1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE = Stream.of(
            Arrays.stream(ABOVE_ADJACENTS),
            Arrays.stream(ABOVE_DIAGONALS),
            Stream.of(new BlockPos(0, 1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_AND_ABOVE = Stream.of(
            Arrays.stream(HORIZONTAL),
            Arrays.stream(ABOVE)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW_ADJACENTS = Stream.of(
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, -1),
            new BlockPos(1, -1, 0),
            new BlockPos(0, -1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW_DIAGONALS = Stream.of(
            new BlockPos(-1, -1, -1),
            new BlockPos(-1, -1, 1),
            new BlockPos(1, -1, -1),
            new BlockPos(1, -1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW = Stream.of(
            Arrays.stream(BELOW_ADJACENTS),
            Arrays.stream(BELOW_DIAGONALS),
            Stream.of(new BlockPos(0, -1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] ADJACENTS_AND_DIAGONALS = Stream.of(
            Arrays.stream(ABOVE),
            Arrays.stream(HORIZONTAL),
            Arrays.stream(BELOW)
    ).flatMap(a -> a).toArray(BlockPos[]::new);


    static public final BlockPos[] ADJACENTS_AND_BELOW_ADJACENTS = Stream.of(
            Arrays.stream(ADJACENTS),
            Arrays.stream(BELOW_ADJACENTS)
    ).flatMap(a -> a).toArray(BlockPos[]::new);
}
