package ht.treechop.common.util;

import ht.tuber.math.Box3;
import ht.tuber.math.Vector3;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;

public enum FaceShape {
    DOWN(
            Direction.DOWN,
            new Vector3(0, 0, 0),
            new Vector3(1, 0, 0),
            new Vector3(1, 0, 1),
            new Vector3(0, 0, 1)
    ),
    UP(
            Direction.UP,
            new Vector3(0, 1, 0),
            new Vector3(1, 1, 0),
            new Vector3(1, 1, 1),
            new Vector3(0, 1, 1)
    ),
    NORTH(
            Direction.NORTH,
            new Vector3(0, 0, 0),
            new Vector3(1, 0, 0),
            new Vector3(1, 1, 0),
            new Vector3(0, 1, 0)
    ),
    SOUTH(
            Direction.SOUTH,
            new Vector3(0, 0, 1),
            new Vector3(1, 0, 1),
            new Vector3(1, 1, 1),
            new Vector3(0, 1, 1)
    ),
    WEST(
            Direction.WEST,
            new Vector3(0, 0, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 1, 1),
            new Vector3(0, 0, 1)
    ),
    EAST(
            Direction.EAST,
            new Vector3(1, 0, 0),
            new Vector3(1, 1, 0),
            new Vector3(1, 1, 1),
            new Vector3(1, 0, 1)
    );

    private final Box3 faceBox;
    private final Direction direction;

    private static final EnumMap<Direction, FaceShape> fromDirections = new EnumMap<>(Direction.class);

    static {
        fromDirections.put(Direction.DOWN, DOWN);
        fromDirections.put(Direction.UP, UP);
        fromDirections.put(Direction.NORTH, NORTH);
        fromDirections.put(Direction.SOUTH, SOUTH);
        fromDirections.put(Direction.WEST, WEST);
        fromDirections.put(Direction.EAST, EAST);
    }

    FaceShape(Direction direction, Vector3 corner1, Vector3 corner2, Vector3 corner3, Vector3 corner4) {
        this.direction = direction;

        Vector3 depthVector = new Vector3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .scale(-0.01);
        Vector3 pushedCorner1 = corner1.add(depthVector);
        this.faceBox = new Box3(pushedCorner1, corner3);
    }

    public static FaceShape get(Direction direction) {
        return fromDirections.get(direction);
    }

    public AABB toAABB() {
        return new AABB(
                faceBox.getMinX(),
                faceBox.getMinY(),
                faceBox.getMinZ(),
                faceBox.getMaxX(),
                faceBox.getMaxY(),
                faceBox.getMaxZ()
        );
    }

    public Direction getDirection() {
        return direction;
    }
}
