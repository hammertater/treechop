package ht.treechop.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.Arrays;
import java.util.stream.Stream;

public class BlockNeighbors {

    protected final BlockPos[] blocks;

    static public final BlockNeighbors UP = new BlockNeighbors(new BlockPos[]{
            new BlockPos(Direction.UP.getNormal())
    });

    static public final BlockNeighbors DOWN = new BlockNeighbors(new BlockPos[]{
            new BlockPos(Direction.DOWN.getNormal())
    });

    static public final BlockNeighbors HORIZONTAL_ADJACENTS = new BlockNeighbors(new BlockPos[]{
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1)
    });

    static public final BlockNeighbors HORIZONTAL_DIAGONALS = new BlockNeighbors(Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new));

    static public final BlockNeighbors VERTICAL_ADJACENTS = join(UP, DOWN);

    static public final BlockNeighbors ADJACENTS = join(HORIZONTAL_ADJACENTS, VERTICAL_ADJACENTS);

    static public final BlockNeighbors HORIZONTAL = join(HORIZONTAL_ADJACENTS, HORIZONTAL_DIAGONALS);

    static public final BlockNeighbors ABOVE = join(UP, offset(new BlockPos(0, 1, 0), HORIZONTAL));

    static public final BlockNeighbors HORIZONTAL_AND_ABOVE = join(HORIZONTAL, ABOVE);

    static public final BlockNeighbors BELOW = join(DOWN, offset(new BlockPos(0, -1, 0), HORIZONTAL));

    static public final BlockNeighbors ADJACENTS_AND_DIAGONALS = join(ABOVE, HORIZONTAL, BELOW);

    static public final BlockNeighbors ADJACENTS_AND_BELOW = join(ADJACENTS, BELOW);

    public BlockNeighbors(BlockPos[] blocks) {
        this.blocks = blocks;
    }

    public static BlockNeighbors join(BlockNeighbors... neighbors) {
        return new BlockNeighbors(Arrays.stream(neighbors).flatMap(BlockNeighbors::asStream).distinct().toArray(BlockPos[]::new));
    }

    protected static BlockNeighbors offset(Vec3i offset, BlockNeighbors neighbors) {
        return new BlockNeighbors(neighbors.asStream().map(p -> p.offset(offset)).toArray(BlockPos[]::new));
    }

    protected Stream<BlockPos> asStream() {
        return Arrays.stream(blocks);
    }

    public Stream<BlockPos> asStream(BlockPos pos) {
        return Arrays.stream(blocks).map(pos::offset);
    }
}
